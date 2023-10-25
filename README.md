# Citylink- A Bus Management Application

The current bus traversal and management system suffers from various challenges such as
inefficient resource allocation, lack of real-time monitoring, and manual handling of operations,
resulting in delays and inconvenience to passengers. These challenges can be addressed by
implementing a cloud computing-based bus traversal and management system that leverages
advanced technologies to provide a seamless and efficient travel experience.
To address these challenges, a cloud computing-based bus traversal and management system
can provide a comprehensive solution. The proposed system aims to optimize the utilization of
resources such as buses and drivers, reduce waiting times, and enhance the overall customer
experience. It will include features such as the live location of buses, route optimization, and
e-ticketing

# Flowchart of the proposed system
<img width="400" alt="system flowchart" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/cee10346-9975-48bd-aa1c-1c41af775d8b">

# Screenshots of the application

users get an e-mail with a deep link to open the application:

<img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/88bbc9ac-39b0-461a-b52f-8a293166e2d4">

Set of permissions needed:

<img width="400" alt="permissions" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/2c033712-9b46-4406-86e0-e37e958eb8c3">          <img width="400" alt="permission" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/1dbfa5e1-3995-4e24-8365-b0081dca4128">
<img width="400" alt="permission" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/c0843d7c-51c9-4ae0-828f-58b5f0fef78b">        <img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/e054254c-2e83-4f13-b564-4221fdfe6233">

Splash Screen and login action:

<video width="400" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/fcd2a7bf-0971-4a9b-8cce-3b6018c3528c"></video>


User Login and Home Page:

<img width="400" alt="user login" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/335cf9e1-6d4a-40cc-b59c-4ffad7ff97a3">    <img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/75aa80ab-16aa-4497-b23d-d556e1549509">

Users can add money and get access to transaction history:

<img width="250" alt="adding money" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/fe53e3ca-9d5e-46a9-9fcf-2c5042aa5ca3"> <img width="250" alt="Conformaation" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/167f10ed-5331-4f78-8e45-700e52cc7956"> <img width="250" alt="transaction history" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/71a5b1c2-87ea-496c-b860-05a16dc4359b">

Users can be notified about the issues different users face on their routes:

<img width="400" alt="add alert" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/cbc93ac5-c890-4a9c-ac6b-cb934b20aaf6"> <img width="400" alt="adding money" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/40673899-a70e-4c5c-898c-df943e0493ea">
<img width="400" alt="adding alert" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/19367c54-925c-4200-8ba7-1f974298ec4a"> <img width="400" alt="alert list" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/6dc4beea-ed89-4bf5-ad25-5d62d37c6beb">

Users can see bus stops near them:

<img width="400" alt="bus stops nearby" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/b4c92d88-3d15-4ebd-bee9-55bf470b42fc">

When a bus is in the user's vicinity they get notified :

<img width="400" alt="bus is nearby" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/94e8309e-fa30-4b35-80a4-35fed8ea8a3e)">

If the bus keeps on approaching, the user is asked whether they want to board the bus and start tracking their journey:

<img width="250" alt="email to user" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/2f8ed9c7-6f14-470f-97a6-5aa38239a018"> <img width="250" alt="tracking user" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/d025efe9-c9e1-4ec7-969f-e8eacebde844"> <img width="250" alt="background tracking" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/5525a0e3-788a-439f-8515-8642dccf820d">

At the end of the journey, the user is asked whether they want to deboard the bus and stop the tracking service:

<img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/51a88f3a-dbe8-4549-a634-3fc870450675">

The trip summary is displayed and recorded:

<img width="400" alt="trip summary" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/93e35c2e-4043-4c94-9ae3-8fcb9677f795"><img width="400" alt="trip list" src="https://github.com/chirag38-unity/CityLink-Private/assets/90930534/e54f6a9c-afb2-41a2-b1ff-e4d4c93ca03f">

# Methodology

* Research design: Tracking user’s journey and deducting traveling fare from the in-built e-wallet to avoid free
rider problem.
* Participants: The system targets daily commuters of local buses.
* GPS Tracking:
GPS (Global Positioning System) tracking is a technology that uses satellites to determine the precise
location of the user. As the nature of technology is independent of the internet it's preferable.
* In-App Wallets:
An in-app wallet is a digital wallet that allows users to store and manage various payment methods within a
mobile application. In-app wallets are used here to deduct the user’s fare directly and ensure no one can take
advantage of free riding. Users can add their credit or debit cards, bank accounts, or other payment methods
to their in-app wallet while registering.
* User Authorization:
Checking if the user passes certain preconditions like his e-wallet balance should be minimum 50Rs before
boarding the bus. After passing these preconditions the user will be allowed to board the bus and the premises
and his journey tracking will begin.
* BLE:
  Bluetooth Low Energy is preferable because the drawback of Bluetooth messaging is to need to pair devices. BLE can send short messages without the need for pairing.

# Technologies, libraries, and packages used

* Kotlin
* Firebase
* Android Studio
* Cyclic
  
  # Local Setup

1. Fork this repository
2. Clone it in your local system
3. Open Android Studio and select 'Open Project'. You can just browse through the file chooser to the folder where you have cloned the project. The file chooser will show an Android face as the folder icon, which you can select to open the project.
4. Link the project to your Firebase Account and your RESTFUL server (we have included sockets too)
5. After opening the project Android Studio will try to build the project directly. To create it manually, follow the menu path 'Build'/'Make Project', or just click the 'Play' button in the toolbar to build and run it on a mobile device or an emulator. The resulting .apk file will be saved in the 'build/outputs/apk/' subdirectory in the project folder.
6. You can install the .apk file on your device and enjoy its enriching features.

Application link: https://drive.google.com/uc?id=1eu_pLJbrTwRIBrJJTAjE_vP8AS0BR6MV&export=download













