# Citylink- A GPS Based Bus Traversal Application

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
<img width="502" alt="image" src="https://github.com/chirag38-unity/CityLink/assets/90930534/63e8df59-7948-465e-8729-b51839ad2b98">

# Screenshots of the application

users get an e-mail with a deep link to open the application:

<img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink/assets/90930534/b24b075c-ba23-4f22-8ec0-65491936429b">

Set of permissions needed:

<img width="400" alt="permissions" src="https://github.com/chirag38-unity/CityLink/assets/90930534/36d7bcb0-7e7b-48eb-ad6d-0e1d5f319000">          <img width="400" alt="permission" src="https://github.com/chirag38-unity/CityLink/assets/90930534/e568c754-45f4-40f6-94e5-8b42d8b183cb">
<img width="400" alt="permission" src="https://github.com/chirag38-unity/CityLink/assets/90930534/155ac3bc-3de9-475d-a8f2-0d5494314f75">        <img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink/assets/90930534/9bb5d04f-cb17-4faa-b712-2e36593a8c61">

Splash Screen and login action:

<video width="400" src="https://github.com/chirag38-unity/CityLink/assets/90930534/6dab44ee-e6a3-4bc4-b455-2d02b5aa1663"></video>


User Login and Home Page:

<img width="400" alt="user login" src="https://github.com/chirag38-unity/CityLink/assets/90930534/f661840d-93ba-424d-bcf6-f0aa4bf8a41c">    <img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink/assets/90930534/9267b63c-e8fb-4f93-9917-98958738286a">

Users can add money and get access to transaction history:

<img width="250" alt="adding money" src="https://github.com/chirag38-unity/CityLink/assets/90930534/4dd880dd-e182-4a61-86f6-9e24195cb33c"> <img width="250" alt="Conformaation" src="https://github.com/chirag38-unity/CityLink/assets/90930534/7b8b5748-f151-4954-ab30-a3f7d107ebd6"> <img width="250" alt="transaction history" src="https://github.com/chirag38-unity/CityLink/assets/90930534/57e0b27f-0c48-4549-952d-df0d73df55c2">

Users can be notified about the issues different users face on their routes:

<img width="400" alt="add alert" src="https://github.com/chirag38-unity/CityLink/assets/90930534/b9af43b2-ddb8-471d-a296-0b87e9abe6cb"> <img width="400" alt="adding money" src="https://github.com/chirag38-unity/CityLink/assets/90930534/678cd213-1883-4e6e-bb80-5a7b2092e232">
<img width="400" alt="adding alert" src="https://github.com/chirag38-unity/CityLink/assets/90930534/81c744ec-5a91-4a6e-9e7b-9a39914243e3"> <img width="400" alt="alert list" src="https://github.com/chirag38-unity/CityLink/assets/90930534/63d6c174-ebc1-44f3-9c64-e55714178760">

Users can see bus stops near them:

<img width="400" alt="bus stops nearby" src="https://github.com/chirag38-unity/CityLink/assets/90930534/220806a3-33f6-4c35-9614-38ec823c1efc">

When a bus is in the user's vicinity they get notified :

<img width="400" alt="bus is nearby" src="https://github.com/chirag38-unity/CityLink/assets/90930534/edbcdbdc-f510-421e-8844-ef266dc2855a">

If the bus keeps on approaching, the user is asked whether they want to board the bus and start tracking their journey:

<img width="250" alt="email to user" src="https://github.com/chirag38-unity/CityLink/assets/90930534/a9391854-9cd6-42f0-b5dc-654c4509cccf"> <img width="250" alt="tracking user" src="https://github.com/chirag38-unity/CityLink/assets/90930534/d6df9695-edb1-4c32-97ea-6b0f9695e75f"> <img width="250" alt="background tracking" src="https://github.com/chirag38-unity/CityLink/assets/90930534/ed3e9ade-961a-42d3-900d-d12e35805524">

At the end of the journey, the user is asked whether they want to deboard the bus and stop the tracking service:

<img width="400" alt="email to user" src="https://github.com/chirag38-unity/CityLink/assets/90930534/1ceda5b8-066b-4dbb-8f83-56bc8126a91f">

The trip summary is displayed and recorded:

<img width="400" alt="trip summary" src="https://github.com/chirag38-unity/CityLink/assets/90930534/3bd759c0-eb3c-4b78-a424-ef8113863b3e"><img width="400" alt="trip list" src="https://github.com/chirag38-unity/CityLink/assets/90930534/cd0e442c-6064-4cc8-b4a6-a69646644b03">

# Methodology

* Research design: Tracking user’s journey and deducting traveling fare from the in-built e-wallet to avoid free-rider problem.
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













