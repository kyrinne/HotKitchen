# Postgres

## Setting Up a Database

Installation with brew:
```
brew install postgres
```

To start postgresql@18 now and restart at login:
```
brew services start postgresql@18
```

Create your database:
```
initdb <your-db-name>
```

To start your database:
```
pg_ctl -D <your-db-name> -l logfile start
```
If this doesn't work, check the logfile. You might need to set a different port in `<your-db-name>/postgresql.conf`.

## Adding Tables to a Database

Enter interactive mode with `psql` (note the lowercase `d` now):

```
psql -d <your-db-name>
```

Creating a table:

```postgresql
CREATE TABLE users (
    email varchar(100),
    password varchar(100),
    type varchar(50)
);
```


## Clean Up

To stop the database service:
```
pg_ctl stop -D <your-db-name>
```

To fully uninstall, first stop the database service. Then delete the database directory.

Run
```
brew uninstall postgres
brew cleanup
```
This will still leave remnants. If you want to wipe everything (all other databases, too!), proceed with these commands:

```
rm -rf /usr/local/var/postgres
rm /usr/local/var/log/postgres.log
rm -f ~/.psqlrc ~/.psql_history
```

At this stage, it might be a good idea to restart your computer before trying again!