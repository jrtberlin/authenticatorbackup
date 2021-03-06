# Authenticator Backup [ROOT]
This very small app enables users with root access to backup the masterkeys from the Google Authenticator App to migrate to a new device or a different authenticator app.

The app also creates an [andOTP](https://github.com/andOTP/andOTP) backup file to simplify migration. The file is saved to `/sdcard/andOTP_import.json`.

## Google Authenticator DB Schema

```
accounts (_id INTEGER PRIMARY KEY,
          email TEXT NOT NULL,
          secret TEXT NOT NULL,
          counter INTEGER DEFAULT 0,
          type INTEGER,
          provider INTEGER DEFAULT 0,
          issuer TEXT DEFAULT NULL,
          original_name TEXT DEFAULT NULL);
```

#### type INTEGER

|integer value|description|
|--|--|
|0|TOTP|
|1|HOTP|
