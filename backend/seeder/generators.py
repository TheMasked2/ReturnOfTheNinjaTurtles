from datetime import date
from uuid import uuid4
from random import choice, randint

from faker import Faker

fake = Faker()
Faker.seed(694206721173882)

CATEGORIES = [
    (101, "Vinyl Records", 'Classic 12" and 7" vinyls'),
    (102, "CDs", "Compact discs"),
    (103, "Cassettes", "Retro cassette tapes"),
    # (104, "Apparel", "Band tees and hoodies"),
    (105, "Audio Gear", "Headphones and speakers"),
    (106, "Instruments", "Guitars, pedals, and accessories"),
]

PRODUCT_TYPES = [
    "Vinyl",
    "Headphones",
    "Cassette",
    "Bluetooth Speaker",
    "T-Shirt",
    "Mixer",
    "Guitar Pedal",
    "Turntable",
    "Poster",
    "Earbuds",
]

PASSWORD_HASH = "$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm"


def _safe_email(first_name: str, last_name: str, index: int) -> str:
    local = f"{first_name.lower()}.{last_name.lower()}{index}"
    return f"{local}@example.com"


def generate_customers(
    count: int,
) -> list[tuple[str, str, str, str, str, str, str, str, str, str]]:
    customers = []
    for index in range(count):
        first_name = fake.first_name()
        last_name = fake.last_name()
        email = _safe_email(first_name, last_name, index)
        customers.append(
            (
                str(uuid4()),
                email,
                PASSWORD_HASH,
                first_name,
                last_name,
                fake.phone_number(),
                fake.street_address(),
                fake.city(),
                fake.postcode(),
                fake.country(),
            )
        )
    return customers


def generate_products(count: int, start_id: int, category_ids: list[int]) -> tuple[
    list[tuple[int, str, float]],
    list[tuple[int, int]],
    list[tuple[int, int, int, int]],
    list[dict],
]:
    products = []
    product_categories = []
    inventory = []
    mongo_products = []

    for offset in range(count):
        product_id = start_id + offset
        product_type = choice(PRODUCT_TYPES)
        product_name = f"{fake.word().title()} {product_type}"
        base_price = round(
            fake.pyfloat(right_digits=2, positive=True, min_value=10, max_value=299.99),
            2,
        )
        category_id = choice(category_ids)
        quantity_available = randint(0, 1000)
        quantity_reserved = randint(0, min(quantity_available, 50))
        sku = f"MUS-{product_id}-{fake.bothify(text='????').upper()}"

        products.append((product_id, product_name, base_price))
        product_categories.append((product_id, category_id))
        inventory.append(
            (product_id, product_id, quantity_available, quantity_reserved)
        )

        mongo_products.append(
            {
                "product_id": product_id,
                "mongo_product_id": f"p{product_id}",
                "sku": sku,
                "product_name": product_name,
                "description": fake.sentence(nb_words=12),
                "base_price": base_price,
                "specs": fake.catch_phrase(),
                "available_since": fake.date_time_between(
                    start_date="-2y", end_date="now"
                ),
            }
        )

    return products, product_categories, inventory, mongo_products


def generate_reviews(
    count: int, product_ids: list[int], customer_ids: list[str]
) -> list[dict]:
    reviews = []
    for index in range(count):
        reviews.append(
            {
                "reviewId": 1_000_000 + index,
                "productId": choice(product_ids),
                "customerId": choice(customer_ids),
                "rating": randint(1, 5),
                "comment": fake.sentence(nb_words=14),
                "createdAt": fake.date_time_between(start_date="-180d", end_date="now"),
            }
        )
    return reviews
