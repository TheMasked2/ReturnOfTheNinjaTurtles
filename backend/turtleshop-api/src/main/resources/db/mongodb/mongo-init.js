db = db.getSiblingDB("turtleshop");

db.createCollection("reviews");
db.createCollection("products");

db.reviews.createIndex({ productId: 1 });
db.reviews.createIndex({ customerId: 1 });
db.reviews.createIndex({ comment: "text" });

db.products.createIndex({ sku: 1 }, { unique: true });