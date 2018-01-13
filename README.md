# Android Event Reporter

## Business Design
- To design a LBS based Android app for users to report events and search nearby events.

## General Function Instruction
- Login and register: each user can register an account with a unique username and login in to enjoy the app.
- Designed a view to list all events in database according to report time.
- Integrated **Google Map API** to track current user’s location and display nearby hot events as well as navigating to the event.
- Developed a view to show details of the clicked event and make comment or like to it.
- Provided a view to report new event with title, description, image and location.
- Other supported features:
	* Used **Google Firebase (Database and Storage)** to store and manage UGC including comments, images, descriptions, title, geolocations, etc.
	* Integrated in-app advertising (**Google AdMob**) to display Google advertisers and interact with users.

## Infrastructure Design
![infrastructure design](https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/Infrastructure.png)
> Activity and Fragment design

- `MainActivity`: for login and register.
- `EventActivity`: show events in two ways, one for all events list, the other for nearby hot events in Google Map.
- `EventsFragment`: show all events in list view.
- `EventListAdapter`: get data of events and ads, and distribute them to corresponding positions in RecyclerView.
- `EventMapFragment`: show nearby hot events in Google Map with markers and navigate to the event.
- `CommentActivity`: show detail information of each specific event clicked by users, including title, username, time, description, comments, etc. and also make new comment on it.
- `CommentAdapter`: get data of event and comments, and distribute them to corresponding positions in RecyclerView.
- `EventReportActivity`: report new event with title, description, location, image, etc.
- `User`: create instance when registering with attributes of each user.
- `Event`: create instance when reporting a new event with attributes like title, like, comment, etc.
- `Comment`: create instance when making comments to a specific event with attributes like description, username, eventId, like, etc.
- `Utils`: some helper method like encryption of password, calculation of distance between two locations, time transformation, etc.
- `LocationTracker`: get permission from users to use Android system GPS or network to get current location.

## Database Design
Use Google Firebase Database to store and manage UGC:
### users

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/users.png" width="577px" height="277px" alt="users collection design">

> users collection design

### events

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/events.png" width="577px" height="669px" alt="events collection design">

> events collection design

### comments

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/comments.png" width="577px" height="750px" alt="comments collection design">

> comments collection design

## Implementation Details
- About **ANR (application not responding)**: Image and string information of events should be stored **separately in cloud storage(Firebase Storage) and database(only store image link/url)** respectively. When loading events in events list, first loading string information in database like title, username, descriptions…then use `AsyncTask` to download image from cloud storage located by image url in database (in **backend thread**), after finishing downloading, showing the image in **UI thread**.
- How to design **event activity**?
	* Show all events in list view --> `Fragment`
	* Show nearby events in map with markers --> `Fragment`
	* Comment on specific event --> `Activity`
	* **First and second features** are two parallel functions of events showing, also they should be presented **independently and also fast loading**, so it’s appropriate to **use two independent fragments** attached on EventActivity.
	* Since **comments are related to specific event**, and they are not independent, so we should use another **activity to show detail information of a specific event** with title, username, description, time, comments, and in the same time, making comment to it.
- Design EventsFragment and CommentActivity with **RecyclerView**
	* `EventsFragment`: to let users scroll up and down all events and ads together, we need to use RecyclerView instead of a layout (static without function scrollbar)
	* `CommentActivity`: to let users scroll up and down event and all comments together, we need to use RecyclerView instead of a layout (static without function scrollbar, and the size is fixed which may cause users can only see a few comments one time)
- Design pattern
	* **Singleton**: Firebase database uses singleton pattern so that the whole app share one database instance and the same data

## Application Screenshot
### Login and register

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/login.png" width="480px" height="853px" alt="login and register activity">

> login and register activity

### Show all events

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/eventlist.png" width="480px" height="853px" alt="events fragment">

> events fragment

### Event details and make comments

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/comment.png" width="480px" height="853px" alt="comment activity">

> comment activity

### Report new event

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/report.png" width="480px" height="853px" alt="event report activity">

> event report activity

### Show nearby events in Google Map

<img src="https://raw.githubusercontent.com/Wangxh329/AndroidEventReporter/master/EventReporter/doc/map.png" width="480px" height="853px" alt="event map fragment">

> event map fragment