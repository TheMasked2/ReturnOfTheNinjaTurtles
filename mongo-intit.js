db = db.getSiblingDB('turtleshop');

// Create 2 collections
db.createCollection("products_mongo");
db.createCollection("categories_mongo");