import os
import sys
import uuid
import random
from datetime import datetime, timedelta, UTC
import psycopg2
from psycopg2 import extras
from pymongo import MongoClient
from faker import Faker
from pathlib import Path
from dotenv import load_dotenv, find_dotenv

dotenv_path = find_dotenv(usecwd=True)
if not dotenv_path:
    raise FileNotFoundError(".env file not found")

load_dotenv(dotenv_path)

# ==========================================
# 1. SIMULATION CONFIGURATION (VIA ENV VARS)
# ==========================================
NUM_CUSTOMERS = 100000
BATCH_SIZE = 5000
NUM_PRODUCTS = 2500

# Postgres Connection Parameters
DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")  # External mapped port from compose

# MongoDB Connection Parameters
MONGO_USER = os.getenv("MONGO_USER")
MONGO_PASSWORD = os.getenv("MONGO_PASSWORD")
MONGO_HOST = os.getenv("MONGO_HOST")
MONGO_PORT = os.getenv("MONGO_PORT")

fake = Faker()


# ==========================================
# 2. HIGH PERFORMANCE BULK INSERT HELPERS
# ==========================================
def execute_batch_insert(cursor, table, columns, rows):
    """Inserts rows in a single heavily optimized multi-row query block."""
    if not rows:
        return
    col_str = ", ".join(columns)
    placeholders = ", ".join(["%s"] * len(columns))
    values_str = ", ".join([f"({placeholders})" for _ in rows])
    flat_values = [val for row in rows for val in row]
    query = f"INSERT INTO {table} ({col_str}) VALUES {values_str}"
    cursor.execute(query, flat_values)


def execute_batch_insert_returning(cursor, table, columns, rows, id_column):
    """Inserts rows and extracts the natively generated serial primary keys in bulk."""
    if not rows:
        return []
    col_str = ", ".join(columns)
    placeholders = ", ".join(["%s"] * len(columns))
    values_str = ", ".join([f"({placeholders})" for _ in rows])
    flat_values = [val for row in rows for val in row]
    query = f"INSERT INTO {table} ({col_str}) VALUES {values_str} RETURNING {id_column}"
    cursor.execute(query, flat_values)
    return [r[0] for r in cursor.fetchall()]


# ==========================================
# 3. CORE ORCHESTRATION PIPELINE
# ==========================================
def main():
    print("=" * 60)
    print(f"STARTING CORE SIMULATION SEED ENGINE (Target: {NUM_CUSTOMERS} customers)")
    print("=" * 60)

    # Establish Connections
    try:
        pg_conn = psycopg2.connect(
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD,
            host=DB_HOST,
            port=DB_PORT,
        )
        pg_conn.autocommit = False  # Controlled transaction blocks per batch
        pg_curr = pg_conn.cursor()
        print("[✔] Successfully attached to PostgreSQL cluster.")
    except Exception as e:
        print(f"[✘] PostgreSQL connection failure: {e}")
        sys.exit(1)

    try:
        mongo_uri = f"mongodb://{MONGO_USER}:{MONGO_PASSWORD}@{MONGO_HOST}:{MONGO_PORT}/turtleshop?authSource=admin"
        mongo_client = MongoClient(mongo_uri)
        mongo_db = mongo_client["turtleshop"]
        mongo_client.admin.command("ping")
        print("[✔] Successfully attached to MongoDB cluster.")
    except Exception as e:
        print(f"[✘] MongoDB connection failure: {e}")
        pg_conn.close()
        sys.exit(1)

    # ----------------------------------------------------
    # PHASE 1 & 2: RECONCILE SYSTEM ROLES, PAYMENTS & CATALOG
    # ----------------------------------------------------
    print("\nExecuting Phase 1 & 2: Environmental Anchors & Catalog Sync...")

    # System Roles Check
    pg_curr.execute("SELECT role_id FROM SYSTEM_ROLES WHERE name = 'ROLE_USER';")
    role_user_row = pg_curr.fetchone()
    if not role_user_row:
        pg_curr.execute(
            "INSERT INTO SYSTEM_ROLES (name, description) VALUES ('ROLE_USER', 'Standard customer access') RETURNING role_id;"
        )
        role_user_id = pg_curr.fetchone()[0]
    else:
        role_user_id = role_user_row[0]

    # Payment Methods Verification / Seeding
    pg_curr.execute("SELECT payment_method_id FROM PAYMENT_METHOD;")
    pm_rows = pg_curr.fetchall()
    if not pm_rows:
        pm_data = [
            ("Visa", "Credit Card"),
            ("Mastercard", "Credit Card"),
            ("PayPal", "Digital Wallet"),
            ("Stripe", "Direct Debit"),
        ]
        pm_ids = execute_batch_insert_returning(
            pg_curr,
            "PAYMENT_METHOD",
            ["provider", "type"],
            pm_data,
            "payment_method_id",
        )
    else:
        pm_ids = [r[0] for r in pm_rows]

    # Verify existing product volume
    pg_curr.execute("SELECT count(*) FROM PRODUCT;")
    product_count = pg_curr.fetchone()[0]

    # If it only contains the 10 basic testing elements from your migrations, append the music set
    if product_count < 11:
        print(
            f"[!] Catalog contains base seeds ({product_count} items). Appending {NUM_PRODUCTS} music-themed products..."
        )

        # 1. Seed unique Categories (Starting at ID 11 to avoid conflicting with 1-10 from migrations)
        music_categories = [
            (
                11,
                "Vinyl Records",
                "Classic analog vinyl releases and special pressings",
            ),
            (12, "Studio Headphones", "High-fidelity reference monitor headphones"),
            (13, "Microphones", "Condenser, dynamic, and ribbon tracking mics"),
            (14, "Studio Monitors", "Active playback speakers for audio production"),
            (
                15,
                "Mixing Consoles",
                "Analog routing desks and digital control surfaces",
            ),
            (16, "Audio Interfaces", "Low-latency sound cards and pre-amp units"),
            (17, "Synthesizers", "Hardware analog synthesizers and sampler modules"),
            (18, "MIDI Controllers", "Keyboards, pad matrices, and DAW controllers"),
            (
                19,
                "Cables & Accessories",
                "Premium balanced audio connectivity solutions",
            ),
            (20, "DJ Gear", "Turntables, media players, and performance mixers"),
            (
                21,
                "CDs & Music DVDs",
                "Compact discs, box sets, and live concert video releases",
            ),
            (
                22,
                "Electric Guitars",
                "Solid-body, semi-hollow, and hollow-body electric guitars",
            ),
            (
                23,
                "Acoustic Guitars",
                "Steel-string acoustic and classical nylon-string guitars",
            ),
            (
                24,
                "Bass Guitars",
                "4-string, 5-string, electric, and acoustic bass guitars",
            ),
            (
                25,
                "Guitar Amplifiers",
                "Tube, solid-state, and digital modeling amps for guitar and bass",
            ),
            (
                26,
                "Effects Pedals",
                "Stompboxes, multi-effects processors, and pedalboard accessories",
            ),
            (
                27,
                "Acoustic Drums",
                "Complete drum kits, snare drums, cymbals, and acoustic percussion",
            ),
            (
                28,
                "Electronic Drums",
                "Digital drum kits, percussion pads, and drum modules",
            ),
            (
                29,
                "Keyboards & Pianos",
                "Digital pianos, stage keyboards, and arranger workstations",
            ),
            (
                30,
                "PA Systems & Speakers",
                "Active and passive loudspeakers, subwoofers, and portable PA units",
            ),
            (
                31,
                "Stage Lighting & FX",
                "LED fixtures, moving heads, lasers, and smoke machines",
            ),
            (
                32,
                "Music Software",
                "Digital Audio Workstations (DAWs), VST plugins, and virtual instruments",
            ),
            (
                33,
                "Field Recorders",
                "Portable handheld audio recorders and dictaphones",
            ),
            (
                34,
                "Podcasting & Streaming",
                "All-in-one streaming bundles, boom arms, and pop filters",
            ),
            (
                35,
                "Merchandise & Clothing",
                "Band t-shirts, posters, mugs, and music-related lifestyle products",
            ),
        ]

        execute_batch_insert(
            pg_curr,
            "CATEGORY",
            ["category_id", "name", "description"],
            music_categories,
        )

        # 2. Generate 2,500 Product Rows starting at ID 11
        adjectives = [
            "Retro",
            "Studio",
            "Pro",
            "Wireless",
            "Analog",
            "Digital",
            "High-Fidelity",
            "Vintage",
            "Audiophile",
            "Compact",
            "Premium",
            "Classic",
            "Deluxe",
            "Touring",
        ]

        # Specifieke producttypes gekoppeld aan de juiste categorie ID
        category_product_types = {
            11: ["Vinyl Record LP", "Double Vinyl Album", "Limited Edition Vinyl"],
            12: ["Monitor Headphones", "Over-Ear Studio Headphones", "In-Ear Monitors"],
            13: ["Condenser Microphone", "Dynamic Vocal Mic", "Ribbon Studio Mic"],
            14: [
                "Active Studio Monitor",
                "Powered Reference Speaker",
                "Studio Subwoofer",
            ],
            15: [
                "Analog Mixing Desk",
                "Digital Mixing Console",
                "Compact Control Surface",
            ],
            16: [
                "USB Audio Interface",
                "Thunderbolt Soundcard",
                "Multi-Channel Preamp",
            ],
            17: [
                "Analog Synthesizer",
                "Polyphonic Synthesizer",
                "Desktop Sampler Module",
            ],
            18: [
                "MIDI Keyboard Controller",
                "Pad Matrix Controller",
                "DAW Control Surface",
            ],
            19: [
                "Balanced XLR Cable",
                "Premium Instrument Cable",
                "TRS Patch Cable Pack",
            ],
            20: [
                "Direct-Drive Turntable",
                "DJ Media Player",
                "2-Channel Performance Mixer",
            ],
            21: ["Remastered CD Box Set", "Live Concert DVD", "Gold Edition Audio CD"],
            22: [
                "Solid-Body Electric Guitar",
                "Semi-Hollow Electric Guitar",
                "Custom Shop Electric",
            ],
            23: [
                "Dreadnought Acoustic Guitar",
                "Classical Nylon-String Guitar",
                "Electro-Acoustic Guitar",
            ],
            24: [
                "4-String Electric Bass",
                "5-String Active Bass",
                "Acoustic Bass Guitar",
            ],
            25: [
                "Tube Combo Amplifier",
                "Solid-State Amp Head",
                "Digital Modeling Amp",
            ],
            26: [
                "Overdrive Stompbox",
                "Multi-Effects Processor",
                "Digital Delay Pedal",
            ],
            27: [
                "5-Piece Acoustic Drum Kit",
                "Wooden Snare Drum",
                "Performance Cymbal Pack",
            ],
            28: [
                "Mesh-Head Electronic Drum Kit",
                "Digital Percussion Pad",
                "Drum Trigger Module",
            ],
            29: ["88-Key Digital Piano", "Pro Stage Keyboard", "Arranger Workstation"],
            30: ["Active PA Loudspeaker", "Portable PA System", "Powered PA Subwoofer"],
            31: ["LED Par Can Fixture", "Moving Head Light", "Strobe Laser FX Machine"],
            32: [
                "Digital Audio Workstation (DAW)",
                "Virtual Instrument VST",
                "Mixing Plugin Bundle",
            ],
            33: [
                "Handheld Handy Recorder",
                "Portable Field Recorder",
                "Multitrack Audio Field Unit",
            ],
            34: [
                "All-in-One Streaming Bundle",
                "Broadcast Boom Arm",
                "Studio Pop Filter",
            ],
            35: [
                "Vintage Band T-Shirt",
                "Limited Edition Tour Poster",
                "Artist Ceramic Mug",
            ],
        }

        product_rows = []
        category_mappings = []
        inventory_rows = []
        mongo_products_batch = []

        # Range shifts from ID 11 to 2510 to preserve your migration database records
        for prod_id in range(11, 11 + NUM_PRODUCTS):
            # Kies een willekeurige categorie uit de volledige nieuwe lijst (11 t/m 35)
            assigned_cat = random.randint(11, 35)

            # Pak een producttype dat specifiek bij deze categorie hoort
            g_type = random.choice(category_product_types[assigned_cat])
            adj = random.choice(adjectives)
            model_variant = f"{random.choice(['X', 'MK', 'PRO', 'V', 'HD'])}-{random.randint(100, 999)}"
            name = f"{adj} {g_type} {model_variant}"

            # Realistische prijsbepaling op basis van de categorie
            if assigned_cat in [11, 19, 21, 34, 35]:  # Goedkopere accessoires en media
                price = round(random.uniform(9.99, 89.99), 2)
            elif assigned_cat in [
                15,
                17,
                22,
                25,
                27,
                29,
                30,
            ]:  # High-end hardware en instrumenten
                price = round(random.uniform(249.99, 1899.99), 2)
            else:  # Mid-range studio gear en effecten
                price = round(random.uniform(49.99, 449.99), 2)

            sku = f"MSC-{prod_id:04d}"

            product_rows.append((prod_id, name, price))
            category_mappings.append((prod_id, assigned_cat))
            inventory_rows.append((prod_id, random.randint(100, 1000), 0))

            mongo_products_batch.append(
                {
                    "product_id": prod_id,
                    "mongo_product_id": f"p{prod_id}",
                    "sku": sku,
                    "product_name": name,
                    "description": f"Premium grade performance-ready {name.lower()} built for modern audio environments.",
                    "base_price": float(price),
                    "specs": f"Standard Edition Retail Box, Model Variant {model_variant}, SKU {sku}",
                    "available_since": datetime.now(UTC) - timedelta(days=365),
                    "suggested_products": [],
                }
            )

        # Execute Catalog Injections
        execute_batch_insert(
            pg_curr,
            "PRODUCT",
            ["product_id", "product_name", "base_price"],
            product_rows,
        )
        execute_batch_insert(
            pg_curr,
            "PRODUCT_CATEGORY",
            ["product_id", "category_id"],
            category_mappings,
        )
        execute_batch_insert(
            pg_curr,
            "INVENTORY",
            ["product_id", "quantity_available", "quantity_reserved"],
            inventory_rows,
        )

        # Sync to MongoDB collection
        mongo_db.products.insert_many(mongo_products_batch)
        pg_conn.commit()

        # Re-align Postgres serial sequences so manual inserts do not step on futures
        pg_curr.execute(
            "SELECT setval('product_product_id_seq', (SELECT MAX(product_id) FROM PRODUCT));"
        )
        pg_conn.commit()

        print(
            f"[✔] Successfully populated PRODUCT, CATEGORY, PRODUCT_CATEGORY, INVENTORY, and MongoDB products."
        )
    else:
        print(
            f"[✔] Custom testing catalog already verified ({product_count} products found). Skipping generation..."
        )

    # Fetch pool for relational generation loops (Includes both base migration seeds and new items)
    pg_curr.execute("SELECT product_id, base_price FROM PRODUCT;")
    product_rows = pg_curr.fetchall()
    product_pool = [(row[0], float(row[1])) for row in product_rows]
    print(
        f"[✔] Catalog ready. {len(product_pool)} items loaded into operational memory pool."
    )

    # ----------------------------------------------------
    # PHASE 3: DISTRIBUTED STATE CHUNKING ENGINE
    # ----------------------------------------------------
    print(
        f"\nExecuting Phase 3: Generating Behavioral Records in blocks of {BATCH_SIZE}..."
    )

    total_processed = 0

    while total_processed < NUM_CUSTOMERS:
        current_chunk_size = min(BATCH_SIZE, NUM_CUSTOMERS - total_processed)

        c_rows = []
        usr_rows = []
        wl_cust_ids = []
        order_generation_payloads = []
        cart_generation_payloads = []
        mongo_reviews_batch = []

        for _ in range(current_chunk_size):
            cust_id = str(uuid.uuid4())
            first = fake.first_name()
            last = fake.last_name()
            email = f"{first.lower()}.{last.lower()}.{uuid.uuid4().hex[:6]}@example-gatling.com"
            phone = fake.phone_number()[:50]
            addr = fake.street_address()
            city = fake.city()
            p_code = fake.postcode()[:20]
            country = fake.country()[:100]

            created_at = datetime.now(UTC) - timedelta(days=random.randint(1, 730))

            c_rows.append(
                (
                    cust_id,
                    email,
                    "mock_hashed_password_for_performance",
                    first,
                    last,
                    phone,
                    addr,
                    city,
                    p_code,
                    country,
                    created_at,
                )
            )
            usr_rows.append((cust_id, role_user_id))

            behavior_dice = random.random()

            if behavior_dice < 0.70:
                num_orders = random.randint(1, 3)
                for o_idx in range(num_orders):
                    order_date = created_at + timedelta(
                        days=random.randint(0, 30), hours=random.randint(0, 23)
                    )
                    if order_date > datetime.now(UTC):
                        order_date = datetime.now(UTC)

                    status_dice = random.random()
                    if status_dice < 0.80:
                        status = "DELIVERED"
                    elif status_dice < 0.90:
                        status = "SHIPPED"
                    elif status_dice < 0.95:
                        status = "PENDING"
                    else:
                        status = "CANCELLED"

                    order_generation_payloads.append(
                        {
                            "customer_id": cust_id,
                            "order_date": order_date,
                            "status": status,
                            "shipping_address": f"{addr}, {city}, {p_code}, {country}",
                        }
                    )

            elif behavior_dice < 0.90:
                sub_dice = random.random()
                if sub_dice < 0.50:
                    cart_generation_payloads.append(
                        {
                            "customer_id": cust_id,
                            "status": "ACTIVE",
                            "order_id": None,
                            "created_at": created_at + timedelta(days=1),
                        }
                    )
                else:
                    wl_cust_ids.append((cust_id,))

        execute_batch_insert(
            pg_curr,
            "CUSTOMER",
            [
                "customer_id",
                "email",
                "password",
                "first_name",
                "last_name",
                "phone",
                "address",
                "city",
                "postal_code",
                "country",
                "created_at",
            ],
            c_rows,
        )
        execute_batch_insert(
            pg_curr, "USER_SYSTEM_ROLES", ["customer_id", "role_id"], usr_rows
        )

        if order_generation_payloads:
            orders_insert_data = [
                (p["customer_id"], p["order_date"], p["status"], 0.0)
                for p in order_generation_payloads
            ]
            order_ids = execute_batch_insert_returning(
                pg_curr,
                "ORDERS",
                ["customer_id", "order_date", "status", "total_amount"],
                orders_insert_data,
                "order_id",
            )

            oi_rows = []
            trans_rows = []
            shipment_payloads = []
            converted_cart_payloads = []

            for idx, order_id in enumerate(order_ids):
                payload = order_generation_payloads[idx]
                num_items = random.randint(1, 4)
                chosen_products = random.sample(
                    product_pool, min(num_items, len(product_pool))
                )

                order_total = 0.0
                cart_items_cache = []

                for prod_id, base_price in chosen_products:
                    qty = random.randint(1, 3)
                    order_total += base_price * qty
                    oi_rows.append((order_id, prod_id, qty))
                    cart_items_cache.append((prod_id, qty))

                pg_curr.execute(
                    "UPDATE ORDERS SET total_amount = %s WHERE order_id = %s;",
                    (order_total, order_id),
                )

                t_status = (
                    "SUCCESS"
                    if payload["status"] in ["DELIVERED", "SHIPPED", "PENDING"]
                    else "FAILED"
                )
                trans_rows.append(
                    (
                        order_id,
                        random.choice(pm_ids),
                        order_total,
                        t_status,
                        payload["order_date"],
                    )
                )

                if payload["status"] in ["DELIVERED", "SHIPPED", "PENDING"]:
                    shipment_payloads.append(
                        {
                            "order_id": order_id,
                            "method": random.choice(
                                ["DHL", "FedEx", "UPS", "Standard"]
                            ),
                            "address": payload["shipping_address"],
                            "status": payload["status"],
                            "base_date": payload["order_date"],
                        }
                    )

                converted_cart_payloads.append(
                    {
                        "customer_id": payload["customer_id"],
                        "status": "CONVERTED",
                        "order_id": order_id,
                        "created_at": payload["order_date"]
                        - timedelta(minutes=random.randint(5, 60)),
                        "items": cart_items_cache,
                    }
                )

                if payload["status"] == "DELIVERED" and random.random() < 0.30:
                    reviewed_prod = chosen_products[0][0]
                    mongo_reviews_batch.append(
                        {
                            "reviewId": random.randint(100000, 99999999),
                            "productId": reviewed_prod,
                            "customerId": payload["customer_id"],
                            "rating": random.choice([4, 5, 5, 5, 3]),
                            "comment": f"Performance testing review for product reference payload {reviewed_prod}.",
                            "createdAt": payload["order_date"]
                            + timedelta(days=random.randint(1, 5)),
                        }
                    )

            execute_batch_insert(
                pg_curr, "ORDER_ITEM", ["order_id", "product_id", "quantity"], oi_rows
            )
            execute_batch_insert(
                pg_curr,
                "TRANSACTION",
                [
                    "order_id",
                    "payment_method_id",
                    "amount",
                    "status",
                    "transaction_date",
                ],
                trans_rows,
            )

            if shipment_payloads:
                ship_data = [
                    (s["order_id"], s["method"], s["address"])
                    for s in shipment_payloads
                ]
                shipment_ids = execute_batch_insert_returning(
                    pg_curr,
                    "SHIPMENT",
                    ["order_id", "shipment_method", "shipping_address"],
                    ship_data,
                    "shipment_id",
                )

                ship_log_rows = []
                for s_idx, s_id in enumerate(shipment_ids):
                    s_meta = shipment_payloads[s_idx]
                    b_date = s_meta["base_date"]

                    ship_log_rows.append(
                        (
                            s_id,
                            "PENDING",
                            b_date + timedelta(minutes=15),
                            "Order verified and processed.",
                        )
                    )
                    if s_meta["status"] in ["SHIPPED", "DELIVERED"]:
                        ship_log_rows.append(
                            (
                                s_id,
                                "SHIPPED",
                                b_date + timedelta(hours=12),
                                "Handed over to carrier network.",
                            )
                        )
                    if s_meta["status"] == "DELIVERED":
                        ship_log_rows.append(
                            (
                                s_id,
                                "DELIVERED",
                                b_date + timedelta(days=2),
                                "Consignment delivered to doorstep.",
                            )
                        )

                execute_batch_insert(
                    pg_curr,
                    "SHIPMENT_STATUS_LOG",
                    ["shipment_id", "status", "status_change_date", "notes"],
                    ship_log_rows,
                )

            if converted_cart_payloads:
                cart_data = [
                    (c["customer_id"], c["status"], c["order_id"], c["created_at"])
                    for c in converted_cart_payloads
                ]
                cart_ids = execute_batch_insert_returning(
                    pg_curr,
                    "CART",
                    ["customer_id", "status", "order_id", "created_at"],
                    cart_data,
                    "cart_id",
                )

                ci_rows = []
                for c_idx, c_id in enumerate(cart_ids):
                    for p_id, qty in converted_cart_payloads[c_idx]["items"]:
                        ci_rows.append((c_id, p_id, qty))
                execute_batch_insert(
                    pg_curr, "CART_ITEM", ["cart_id", "product_id", "quantity"], ci_rows
                )

        if cart_generation_payloads:
            c_active_data = [
                (c["customer_id"], c["status"], c["order_id"], c["created_at"])
                for c in cart_generation_payloads
            ]
            cart_ids = execute_batch_insert_returning(
                pg_curr,
                "CART",
                ["customer_id", "status", "order_id", "created_at"],
                c_active_data,
                "cart_id",
            )
            ci_active_rows = []
            for c_id in cart_ids:
                num_items = random.randint(1, 3)
                items = random.sample(product_pool, min(num_items, len(product_pool)))
                for p_id, _ in items:
                    ci_active_rows.append((c_id, p_id, random.randint(1, 2)))
            execute_batch_insert(
                pg_curr,
                "CART_ITEM",
                ["cart_id", "product_id", "quantity"],
                ci_active_rows,
            )

        if wl_cust_ids:
            wl_ids = execute_batch_insert_returning(
                pg_curr, "WISHLIST", ["customer_id"], wl_cust_ids, "wishlist_id"
            )
            wli_rows = []
            for w_id in wl_ids:
                num_items = random.randint(1, 4)
                items = random.sample(product_pool, min(num_items, len(product_pool)))
                for p_id, _ in items:
                    wli_rows.append(
                        (
                            w_id,
                            p_id,
                            datetime.now(UTC) - timedelta(days=random.randint(1, 10)),
                        )
                    )
            execute_batch_insert(
                pg_curr,
                "WISHLIST_ITEM",
                ["wishlist_id", "product_id", "added_at"],
                wli_rows,
            )

        if mongo_reviews_batch:
            mongo_db.reviews.insert_many(mongo_reviews_batch)

        pg_conn.commit()
        total_processed += current_chunk_size
        print(
            f" -> Flushed total of {total_processed}/{NUM_CUSTOMERS} fully mapped testing users..."
        )

    pg_curr.close()
    pg_conn.close()
    mongo_client.close()

    print("=" * 60)
    print("[✔] TRANSACTIONAL SIMULATION SEED COMPLETED SUCCESSFULLY.")
    print("=" * 60)


if __name__ == "__main__":
    main()
