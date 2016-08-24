# Android Demo

Firebase Storage Demo application presented in Helsinki Android meetup.
I'm not a full-time Android developer, so please forgive me about the demo code quality :)

## Firebase console configurations

### Authentication

* Enable the Anonymous Sign-In provider

### Database rules

* Create the following database rules:

```json
{
  "rules": {
    ".read": false,
    ".write": false,
    "uploads": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$id": {
        ".validate": "newData.hasChildren(['dUrl', 'nickname', 'uid', 'createdAt'])",
        ".indexOn": ["createdAt"]
      }
    },
    "user-uploads": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "users": {
      ".write": "auth != null",
      "$uid": {
        ".read": "$uid === auth.uid"
      }
    }
  }
}
```

### Storage rules

* Create the following storage rules:

```
service firebase.storage {
  match /b/realtrans-6e37f.appspot.com/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
