# Postgres

## Setting Up a Database

Installation with brew:
```
brew install postgres
```
This will also create a default database cluster in `/opt/homebrew/var/postgresql@18`.

To start postgresql@18 now and restart at login:
```
brew services start postgresql@18
```

DO NOT run `initdb` as this will create a database cluster rather than a database!

```
initdb <your-db-cluster-name>
```

Instead, create your database:

```
createdb <your-db-name>
```

## Adding Tables to a Database

Enter interactive mode with `psql`:

```
psql -d <your-db-name>
```

### Creating a simple table:

```postgresql
CREATE TABLE users (
    email varchar(100),
    password varchar(100),
    userType varchar(50)
);
```
### Creating tables with a n:n relationship, joined via a third helper table:

```postgresql
CREATE TABLE categories(
    categoryId INTEGER PRIMARY KEY,
    title varchar(200),
    description varchar(2000)
);
```

```postgresql
CREATE TABLE meals(
    mealId     INTEGER PRIMARY KEY,
    title      VARCHAR(100),
    price      NUMERIC(10, 2),
    imageUrl   VARCHAR(300),
    categoryId INTEGER
);
```

```postgresql
CREATE TABLE meal_category(
    mealId INTEGER REFERENCES meals ON DELETE CASCADE,
    categoryId INTEGER REFERENCES categories ON DELETE CASCADE,
    UNIQUE (mealId, categoryId)
);
```

## Modifying a table:

```postgresql
ALTER TABLE users
ADD COLUMN name varchar(100),
ADD COLUMN phone varchar(30),
ADD COLUMN address varchar(200);
```

## Creating a user for the app to use to log in:

```postgresql
CREATE USER test WITH
    PASSWORD 'superSecretPassword'
```
(When using `CREATE ROLE`, `LOGIN` needs to be added.)

Grant the user access to the database (adjust permissions as required):

```postgresql
GRANT SELECT, INSERT, UPDATE ON TABLE users TO test; 
```

## Clean Up

To stop the database service:
```
brew services stop postgresql
```

To fully uninstall, first stop the database service. Then delete the database directory.

Run
```
brew uninstall postgres
brew cleanup
```
This will still leave remnants. If you want to wipe everything (all other databases, too!), proceed with these commands:

```
rm -rf /opt/homebrew/var/postgresql@18
```

At this stage, it might be a good idea to restart your computer before trying again!