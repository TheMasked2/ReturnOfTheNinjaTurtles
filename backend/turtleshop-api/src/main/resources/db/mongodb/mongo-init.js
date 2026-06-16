db = db.getSiblingDB("turtleshop");

db.createCollection("reviews");
db.createCollection("products");

db.reviews.createIndex({ productId: 1 });
db.reviews.createIndex({ customerId: 1 });
db.reviews.createIndex({ comment: "text" });

db.products.createIndex({ product_id: 1 }, { unique: true });
db.reviews.createIndex({ productId: 1, createdAt: -1 });
db.reviews.createIndex({ customerId: 1, createdAt: -1 });

db.products.createIndex({ sku: 1 }, { unique: true });

// Seeding data
db.products.insertMany([
  {
    product_id: 1,
    mongo_product_id: "p1",
    sku: "TMNT-001",
    product_name: "Retro Vinyl Record",
    description: "180g heavyweight vinyl featuring classic turtle beats.",
    base_price: 24.99,
    specs: "LP, 12-inch, gatefold sleeve",
    available_since: new Date("2024-02-01"),
    suggested_products: ["TMNT-003", "TMNT-007"]
  },
  {
    product_id: 2,
    mongo_product_id: "p2",
    sku: "TMNT-002",
    product_name: "Studio Headphones",
    description: "Over-ear headphones tuned for crisp beats and deep bass.",
    base_price: 79.99,
    specs: "Noise isolating, 40mm drivers",
    available_since: new Date("2024-02-05"),
    suggested_products: ["TMNT-004", "TMNT-009"]
  },
  {
    product_id: 3,
    mongo_product_id: "p3",
    sku: "TMNT-003",
    product_name: "Portable Bluetooth Speaker",
    description: "Compact wireless speaker with powerful turtle bass.",
    base_price: 39.99,
    specs: "10-hour playtime, water-resistant",
    available_since: new Date("2024-02-08"),
    suggested_products: ["TMNT-002", "TMNT-010"]
  },
  {
    product_id: 4,
    mongo_product_id: "p4",
    sku: "TMNT-004",
    product_name: "DJ Turntable",
    description: "Entry-level turntable for spinning vinyl in style.",
    base_price: 129.99,
    specs: "Belt-drive, USB output",
    available_since: new Date("2024-02-12"),
    suggested_products: ["TMNT-001", "TMNT-006"]
  },
  {
    product_id: 5,
    mongo_product_id: "p5",
    sku: "TMNT-005",
    product_name: "Cassette Tape Set",
    description: "Four-pack of retro cassette tapes with turtle artwork.",
    base_price: 19.99,
    specs: "Color cassette shell, 90 minutes",
    available_since: new Date("2024-02-15"),
    suggested_products: ["TMNT-001", "TMNT-008"]
  },
  {
    product_id: 6,
    mongo_product_id: "p6",
    sku: "TMNT-006",
    product_name: "Guitar Effect Pedal",
    description: "Distortion pedal with mutant overdrive tones.",
    base_price: 54.99,
    specs: "True bypass, LED indicator",
    available_since: new Date("2024-02-18"),
    suggested_products: ["TMNT-002", "TMNT-009"]
  },
  {
    product_id: 7,
    mongo_product_id: "p7",
    sku: "TMNT-007",
    product_name: "Record Cleaning Kit",
    description: "Complete kit for keeping vinyl records sounding fresh.",
    base_price: 17.99,
    specs: "Brush, fluid, microfiber cloth",
    available_since: new Date("2024-02-20"),
    suggested_products: ["TMNT-001", "TMNT-004"]
  },
  {
    product_id: 8,
    mongo_product_id: "p8",
    sku: "TMNT-008",
    product_name: "Band Tour Poster",
    description: "Limited edition music poster with ninja turtle art.",
    base_price: 14.99,
    specs: "18x24 inch, matte finish",
    available_since: new Date("2024-02-22"),
    suggested_products: ["TMNT-005", "TMNT-010"]
  },
  {
    product_id: 9,
    mongo_product_id: "p9",
    sku: "TMNT-009",
    product_name: "Wireless Earbuds",
    description: "True wireless earbuds tuned for music and mobility.",
    base_price: 59.99,
    specs: "Bluetooth 5.3, charging case",
    available_since: new Date("2024-02-25"),
    suggested_products: ["TMNT-002", "TMNT-003"]
  },
  {
    product_id: 10,
    mongo_product_id: "p10",
    sku: "TMNT-010",
    product_name: "Music-themed Hoodie",
    description: "Cozy hoodie with album art and headphone graphics.",
    base_price: 44.99,
    specs: "Cotton blend, unisex sizes",
    available_since: new Date("2024-02-28"),
    suggested_products: ["TMNT-008", "TMNT-009"]
  }
]);

db.reviews.insertMany([
  {
    reviewId: 1,
    productId: 1,
    customerId: "00000000-0000-0000-0000-000000000001",
    rating: 5,
    comment: "Love this vinyl sound quality and the retro sleeve design.",
    createdAt: new Date("2024-04-01T10:00:00Z")
  },
  {
    reviewId: 2,
    productId: 2,
    customerId: "00000000-0000-0000-0000-000000000002",
    rating: 4,
    comment: "Headphones are comfortable and bass is nice, great for long sessions.",
    createdAt: new Date("2024-04-02T12:30:00Z")
  },
  {
    reviewId: 3,
    productId: 3,
    customerId: "00000000-0000-0000-0000-000000000003",
    rating: 4,
    comment: "Portable speaker is loud enough and easy to carry around.",
    createdAt: new Date("2024-04-03T14:15:00Z")
  },
  {
    reviewId: 4,
    productId: 4,
    customerId: "00000000-0000-0000-0000-000000000004",
    rating: 5,
    comment: "The DJ turntable looks sharp and works smoothly for beginners.",
    createdAt: new Date("2024-04-04T16:00:00Z")
  },
  {
    reviewId: 5,
    productId: 5,
    customerId: "00000000-0000-0000-0000-000000000005",
    rating: 4,
    comment: "Cassette set is a fun collector item with nice artwork.",
    createdAt: new Date("2024-04-05T09:45:00Z")
  },
  {
    reviewId: 6,
    productId: 6,
    customerId: "00000000-0000-0000-0000-000000000006",
    rating: 5,
    comment: "This pedal gives a gritty tone that really stands out.",
    createdAt: new Date("2024-04-06T11:20:00Z")
  },
  {
    reviewId: 7,
    productId: 7,
    customerId: "00000000-0000-0000-0000-000000000007",
    rating: 5,
    comment: "Record cleaning kit is simple but effective, my vinyl sounds cleaner.",
    createdAt: new Date("2024-04-07T13:30:00Z")
  },
  {
    reviewId: 8,
    productId: 8,
    customerId: "00000000-0000-0000-0000-000000000008",
    rating: 4,
    comment: "Poster is high quality and looks great on the wall.",
    createdAt: new Date("2024-04-08T15:05:00Z")
  },
  {
    reviewId: 9,
    productId: 9,
    customerId: "00000000-0000-0000-0000-000000000009",
    rating: 4,
    comment: "Earbuds are convenient and sound clear for music and calls.",
    createdAt: new Date("2024-04-09T17:10:00Z")
  },
  {
    reviewId: 10,
    productId: 10,
    customerId: "00000000-0000-0000-0000-000000000010",
    rating: 5,
    comment: "Music hoodie is super comfy and the design is awesome.",
    createdAt: new Date("2024-04-10T18:45:00Z")
  }
]);