//TODO fix the bcrypt
db = db.getSiblingDB("kitchensink");

db.users.insertMany
    {
        username: "admin",
        password: "$2a$10$Rk/H7MuV7UYbvG9nPOVJL.cCT2nO9y7vw7YutGZA1L.tu2kucOnmi",
        roles: ["ADMIN"]
    },
    {
        username: "user",
        password: "$2a$10$Rk/H7MuV7UYbvG9nPOVJL.cCT2nO9y7vw7YutGZA1L.tu2kucOnmi",
        roles: ["USER"]
    }
]);

print("Users with roles inserted into the 'users' collection!");
