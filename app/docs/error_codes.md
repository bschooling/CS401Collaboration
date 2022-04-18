# Error Codes

Error codes are four digit numbers that are used to indicate program faults.

They come in the form of `DCE`, where `E` is a two-digit error code identifer.

## Format

||D|C|E|
|:---:|:---:|:---:|:---:|
|DCE|Domain Specifier|Component Specifier|Error Code|
|1299|1|2|99|

# Domain Specifier

|Domain|Specifier|
|:---:|:---:|
|5|User Auth|

# Component Specifier

|Component|Specifier|
|:---:|:---:|
|1|Firebase Database|

## Error Code List

|Code|Domain|Component|Error Code|Notes|
|:---:|:---:|:---:|:---:|:---:|
|5100|User Auth|Firebase Database|00|User successfully created via fire-auth, database user entry creation failure, user deletion from firebase-auth successful.|
|5101|User Auth|Firebase Database|01|User successfully created via fire-auth, database user entry creation failure, user deletion from firebase-auth failed. We have an orphaned user in firebase-auth not present in users collection of firebase database.|
