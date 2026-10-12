# App-specific R8 rules. Library rules ship with the libraries themselves (consumer rules).
# Keep this file minimal and comment every rule with WHY it is needed.

# Keep line numbers for readable Crashlytics stack traces; mapping files are uploaded in CI.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
