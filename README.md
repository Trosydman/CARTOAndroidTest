<p align="center">
<img src="./images/CARTO-android.png" width="500">
</p>

# My CARTO Android Test by Carlos Mesquita

## Introduction
Hi! My name is Carlos Mesquita. I forked the [CARTOAndroidTest](https://github.com/CartoDB/CARTOAndroidTest) in order to apply for the _Android Engineer (Remote)_ position.
I want to highlight that I would have loved to go far beyong in some aspects but due to my current employment, I couldn't expend full time on this test. I hope you can understand.
Apart from that, it has been a fun and challenging technical test. I hope you can appreciate it as much as I did. **Let's GO!**

## Architecture
In this project I made use of the MVVM pattern. The architecture is based on the following parts:
### UI
It is built on top of the **NewMainActivity**, from where we can differentiate 2 Fragments:
  * **MapFragment**: where the map and related views are displayed.
  * **PoisListDialogFragment**: for the list of POIs and the search field.

Both Fragments and the Activity are consuming the same ViewModel: **MainViewModel**. All of them where highly related between them, so I thought it would make sense.

The comunication between the pure UI elements (Activity + Fragments) and the ViewModel is made by the **unidirectional event-state data flow pattern**. This pattern allows to separate the UI events (from Activity and Fragments) from the UI states (requested by the MainViewModel). It highly achieves to decouple the UI logic and the business logic.

Both of this data flows are channeled between 2 different types of objects: **kotlinx.coroutines.channels.Channel** (for UI events) and **kotlinx.coroutines.flow.MutableSharedFlow** (for UI states through all Fragments and the Activity).

### Repository + DataSource

It was built a **PoiRepository** which consumes from a **remote data source** that provides all POIs information from the given CARTO endpoint using **Retrofit**.

A separate ***DataSource*** was created because it was planned to create a _LocalDataSource_ to be able to save POIs locally in a Room database. Unfortunatelly this was not possible to acomplish in the given time.

### Data Models
In order to keep the UI model (domain model) separate from any DTO model, I created 2 different types of models:
  * **Poi** as domain model
  * **PoiDTO** as the remote source model
Both of them make use of their own mappers which are used in the Repository layer to map the DTOs to the domain model when retrieving the list of POIs.

## Missing parts

As explained above, due to my current situation I didn't have much more time to fulfill with all features I wanted to implement in this project, e.g.:
- **LocalDataSource** to save POIs locally
- **Depper testing**
- **Better design system**: with a proper day/night theme crafted by custom and unique colors, text appearances, fonts, dimensions, animations... to highlight the CARTO brand
- **Code beautyfication**: not only correct formatting and clarity or removal of old/unused files and resources, but also proper KDocs to document properly the code.


## APK and Conclusion
This is only the pick of the iceberg! There is way more things that I am thrilled to share with you and also to learn from you.

As required, here you can find the [**APK**](https://drive.google.com/file/d/1YivHqq9CjoDb5UlNyDr6CR3pSIwH8Zmo/view?usp=sharing) of my proposal.


I hope to speak soon with you.
Cheers,

**Carlos Mesquita**



- - - -



# CARTO Android Test

We have developed an Android app showing traffic cameras from a specific dataset. The app positions the traffic cameras on a map according to their latitude and longitude.

The traffic cameras are in Sydney and you are probably not there but don't worry, we have mocked the GPS location for you so we can pretend you are :)

The app is working perfectly but the code smells. There are no design patterns, architecture, good practices, SOLID principles, etc. Please, we need your help. Do you want to accept the challenge?

Hereafter, we will refer to the traffic cameras as Points of Interest (POIs).

Before starting the test, please visit our [open positions](https://carto.com/careers/#open-positions) and fill in the form. If there are no open positions but you still would like to apply, please send an email to [mdominguez@carto.com](mailto:mdominguez@carto.com), she'll get back to you asap.

We'd like you to send us your app in the next 15 days. If you have any question about the test, please don't hesitate to [contact us (jguerrero@carto.com)](mailto:jguerrero@carto.com).

## Screenshots

### Home & POI search

<p align="left">
<img src="./images/Home.jpg" width="280">
<img src="./images/Search Poi.jpg" width="280">
</p>

- <b>Home Screen</b>: Show the traffic cameras and the blue dot with your GPS location (if it's enabled).
- <b>POI Search Screen</b>: Show the traffic cameras list. Filters by *title* and *description* are not implemented. It would be great if you develop this feature.

### POI Detail & POI PreRoute & POI Route (without GPS Location)

<p align="left">
<img src="./images/Poi Detail (No Location).jpg" width="280">
<img src="./images/Poi Pre-Route (No Location).jpg" width="280">
<img src="./images/Poi Route (No Location).jpg" width="280">
</p>

This is how the app should behave when you choose a traffic camera (by clicking on a marker on the map or by using the search bar) and your GPS location is <b>disabled</b>:

- <b>POI Detail Screen</b>: Show the traffic camera details. If you click on "DIRECTIONS", the next screen will be the POI PreRoute screen, described below.
- <b>POI PreRoute Screen</b>: Choose another traffic camera by clicking on a marker.
- <b>POI Route Screen</b>: Draw the route between the selected traffic cameras, including the distance and time of the route. If you choose another traffic camera, it will be set as the <b>origin</b> of the route. The "START" button should be disabled.

### POI Detail & POI Route & POI Navigation (with GPS Location)

<p align="left">
<img src="./images/Poi Detail (Location).jpg" width="280">
<img src="./images/Poi Route (Location).jpg" width="280">
<img src="./images/Poi Navigation (Location).jpg" width="280">
</p>

This is how the app should behave when you choose a traffic camera (by clicking on a marker on the map or by using the search bar) and your GPS location is <b>enabled</b>.

- <b>POI Route Screen</b>: Draw the route between your location and the selected traffic camera, including the distance and time of the route. If you click the "START" button, the next screen will be POI Navigation Screen, described below.
- <b>POI Navigation</b>: Show a panel with the text "YOU ARE NAVIGATING" and another one with the button "FINISH". If you click on this button, you will return to the previous screen.

## Considerations
- You are free to change the specific map library to work with. If you use Google Maps, you can use our apikey included in the project.
- You are free to modify the UI and the flow between screens.
- The dataset is accessible through a GET over `https://javieraragon.carto.com/api/v2/sql?q=SELECT id, direction, href as image, region, title, view as description, ST_X(the_geom) as longitude, ST_Y(the_geom) as latitude FROM ios_test`. The query param `q` is used to pass a SQL query to extract the data from the dataset. Under the hood, it will query a Postgres + PostGIS which enables multiple ways to get info using SQL and special spatial functions.
- If for whatever reason you are not able to complete the test, don't worry, we'll review it anyway.

## We will pay special attention to...
- Clean Code and scalable architecture.
- Design pattern to handle the views states.
- Good practices, SOLID principles, etc.
- Creativity of the solution.
- Performance!
- Any extra details in terms of functionality, UI/UX or performance. Think of this task as an opportunity to showcase all of your technical skills.
- Documentation (use the README for this), explaining your solution and your technical decisions. If you decide to leave out certain things, tell us why.

## You will get bonus points if...
The following features are not mandatory, but will definitely give you bonus points! 
- The app also works <b>offline</b>.
- You use our [mobile sdk](https://carto.com/developers/mobile-sdk/). If you end up working at CARTO, you will work with it a lot.
- The app is written as production code.
- You include unit and functional tests.
- Animations. For example, you could use *BottomSheetDialogFragment* to show information on the map.
- The app works in landscape mode but without changing the design (we know it's a lot of work).
- <b>IMPORTANT</b>: You can substitute any of the points mentioned in the bonus section with your personal code repositories. For example, if you already have a repository where you demonstrate how to work with an offline app, simply include the URL and let us know where to find the relevant bits.


## Ok, you've built the app, what should you do now?
Invite [@jaimegc](https://github.com/jaimegc) to the repo of your app. Please add a link to the APK in the README to help us with the installation.
