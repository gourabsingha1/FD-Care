# FD-Care
FD-Care is an app for detecting fall and sending notifications to the saved emergency contact.

# Description
1) One to one mapping. Directed graph. Patient -> Caretaker. A caretaker can be a patient of another caretaker
but a patient cannot have multiple caretakers or vice versa
2) Patient signs up with their details + their caretaker's details. caretaker signs up with their details
3) When one of the patient or the caretaker registers, but the other hasn't or there is no link between them yet,
then a popup will show in home activity.
4) Accelerometer sends x, y, z coordinates to ML model
5) ML model returns true if patient falls down
6) When patient falls down, the patient uses their saved caretakerToken to send the notification to their caretaker.
7) A notification is received on caretaker's device + device rings until caretaker presses stop button or just close the app.
8) When you first open the app, a splashscreen will appear, during that time, it will check whether the user is
a patient or a caretaker or not yet registered. according to that, you will be redirected to the right activity.

# Conditions
1) Caretaker must not put their phone in "Do not disturb" mode.
2) "Pause when app is unused" must be off

## Installations
1. Clone the repository: git clone https://github.com/gourabsingha1/FD-Care.git
2. Open the project in Android Studio
3. Build and run the app on your Android device
4. Alternatively, you can install the apk from [here](https://drive.google.com/file/d/19enzprKiBJ0ehA8azwsbhQRxNeN2nsko/view?usp=sharing)
