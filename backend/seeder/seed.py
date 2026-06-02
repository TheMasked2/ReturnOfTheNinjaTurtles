import os
from pathlib import Path
from time import perf_counter

from dotenv import load_dotenv
from pymongo import MongoClient
from psycopg import connect

from generators import (
    CATEGORIES,
    generate_customers,
    generate_products,
    generate_reviews,
)

CONFIG = {
    "CUSTOMER_COUNT": 100000,
    "PRODUCT_COUNT": 100000,
    "REVIEW_COUNT": 150000,
    "BATCH_SIZE": 5000,
    "START_PRODUCT_ID": 1000,
}


def load_environment() -> None:
    seeder_dir = Path(__file__).resolve().parent
    repo_root = seeder_dir.parent.parent
    dotenv_path = repo_root / ".env"
    if not dotenv_path.exists():
        dotenv_path = seeder_dir / ".env"
    if not dotenv_path.exists():
        raise FileNotFoundError(
            "Unable to find .env in repo root or backend/seeder directory"
        )

    load_dotenv(dotenv_path)


def build_mongo_uri() -> str:
    uri = os.getenv("MONGO_URI")
    if uri:
        return uri

    mongo_user = os.getenv("MONGO_USER")
    mongo_password = os.getenv("MONGO_PASSWORD")
    mongo_host = os.getenv("MONGO_HOST", "localhost")
    mongo_port = os.getenv("MONGO_PORT", "27017")
    database = os.getenv("PG_DATABASE", "turtleshop")

    if not mongo_user or not mongo_password:
        raise ValueError("Mongo credentials are missing in the environment")

    return f"mongodb://{mongo_user}:{mongo_password}@{mongo_host}:{mongo_port}/{database}?authSource=admin"


def get_postgres_connection():
    # Fixed to match the exact keys present in your .env file
    return connect(
        dbname=os.getenv("PG_DATABASE", "turtleshop"),
        user=os.getenv("PG_USER", "turtleadmin"),
        password=os.getenv("PG_PASSWORD", "splinter"),
        host=os.getenv("PG_HOST", "localhost"),
        port=os.getenv("PG_PORT", "5437"),
    )


def seed_categories(cursor):
    cursor.executemany(
        "INSERT INTO CATEGORY (category_id, name, description) VALUES (%s, %s, %s) ON CONFLICT (category_id) DO NOTHING",
        CATEGORIES,
    )


def seed_customers(cursor, count):
    customer_ids = []
    for offset in range(0, count, CONFIG["BATCH_SIZE"]):
        batch_size = min(CONFIG["BATCH_SIZE"], count - offset)
        customers = generate_customers(batch_size)
        customer_ids.extend([customer[0] for customer in customers])
        cursor.executemany(
            "INSERT INTO CUSTOMER (customer_id, email, password, first_name, last_name, phone, address, city, postal_code, country) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) ON CONFLICT DO NOTHING",
            customers,
        )
        print(f"Inserted {offset + batch_size} / {count} customers", end="\r")
    print()
    return customer_ids


def seed_products(cursor, mongo_collection, count, category_ids):
    product_ids = []
    for offset in range(0, count, CONFIG["BATCH_SIZE"]):
        batch_size = min(CONFIG["BATCH_SIZE"], count - offset)
        products, product_categories, inventory, mongo_products = generate_products(
            batch_size,
            CONFIG["START_PRODUCT_ID"] + offset,
            category_ids,
        )

        product_ids.extend([product[0] for product in products])

        cursor.executemany(
            "INSERT INTO PRODUCT (product_id, product_name, base_price) VALUES (%s, %s, %s) ON CONFLICT DO NOTHING",
            products,
        )
        cursor.executemany(
            "INSERT INTO PRODUCT_CATEGORY (product_id, category_id) VALUES (%s, %s) ON CONFLICT DO NOTHING",
            product_categories,
        )
        cursor.executemany(
            "INSERT INTO INVENTORY (inventory_id, product_id, quantity_available, quantity_reserved) VALUES (%s, %s, %s, %s) ON CONFLICT DO NOTHING",
            inventory,
        )

        mongo_collection.insert_many(mongo_products, ordered=False)
        print(f"Inserted {offset + batch_size} / {count} products", end="\r")
    print()
    return product_ids


def seed_reviews(mongo_collection, count, product_ids, customer_ids):
    for offset in range(0, count, CONFIG["BATCH_SIZE"]):
        batch_size = min(CONFIG["BATCH_SIZE"], count - offset)
        reviews = generate_reviews(batch_size, product_ids, customer_ids)
        mongo_collection.insert_many(reviews, ordered=False)
        print(f"Inserted {offset + batch_size} / {count} reviews", end="\r")
    print()


def sync_sequences(cursor):
    cursor.execute(
        "SELECT setval(pg_get_serial_sequence('product', 'product_id'), (SELECT MAX(product_id) FROM product));"
    )
    cursor.execute(
        "SELECT setval(pg_get_serial_sequence('inventory', 'inventory_id'), (SELECT MAX(inventory_id) FROM inventory));"
    )
    cursor.execute(
        "SELECT setval(pg_get_serial_sequence('category', 'category_id'), (SELECT MAX(category_id) FROM category));"
    )


def main():
    load_environment()
    mongo_uri = build_mongo_uri()

    print("Connecting to MongoDB...")
    mongo_client = MongoClient(mongo_uri)
    mongo_db = mongo_client.get_database()
    mongo_products = mongo_db["products"]
    mongo_reviews = mongo_db["reviews"]

    print("Connecting to Postgres...")
    with get_postgres_connection() as conn:
        with conn.cursor() as cursor:
            print("Seeding categories...")
            seed_categories(cursor)
            conn.commit()

            category_ids = [category[0] for category in CATEGORIES]

            print(f'Seeding {CONFIG["CUSTOMER_COUNT"]} customers...')
            customer_ids = seed_customers(cursor, CONFIG["CUSTOMER_COUNT"])
            conn.commit()

            print(f'Seeding {CONFIG["PRODUCT_COUNT"]} products...')
            product_ids = seed_products(
                cursor, mongo_products, CONFIG["PRODUCT_COUNT"], category_ids
            )
            conn.commit()

            print(f'Seeding {CONFIG["REVIEW_COUNT"]} reviews...')
            seed_reviews(
                mongo_reviews, CONFIG["REVIEW_COUNT"], product_ids, customer_ids
            )

            print("Syncing Postgres sequences...")
            sync_sequences(cursor)
            conn.commit()

    mongo_client.close()
    print("Seeding complete.")


if __name__ == "__main__":
    main()