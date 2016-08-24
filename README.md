# Android Demo

Firebase Storage Demo application presented in Helsinki Android meetup.
I'm not a full-time Android developer, so please forgive me about the demo code quality :)

## Demo App

You can download the APK for the demo app from here: https://github.com/jerryjj/firebase-storage-demo-android/raw/master/debug-release/app-debug.apk
The source code is available under the RealTrans-folder.

## Firebase console configurations

Download the Android config file (google-services.json) from the console and put it in the RealTrans/app/ -folder.

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
        ".indexOn": ["createdAt", "starCount"]
      }
    },
    "user-uploads": {
      "$uid": {
        ".read": "auth != null",
        ".write": "$uid === auth.uid",
        "$upload_id": {
          // This is not the best way to do this, but didn't find better one yet
          ".write": "auth != null && (data.exists() || $uid === auth.uid)"
        }
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
