# HVI_Routes_Between_Two_Points
## Overview
This app enables users to get a highlighted route between two locations on a map using [Google Places API](https://developers.google.com/maps/documentation/places/web-service/overview) and [Google Directions API](https://developers.google.com/maps/documentation/directions/overview). 
Additionally the app also tells users the distance between the two points and the estimated duration it might take to travel.

##### Table of Contents  
[Screenshots](#screenshots)  
[Video](#video)

## Screenshots
![Screenshot1](/ss1.jpg)<p>![Screenshot2](/ss2.jpg)<p>![Screenshot3](/ss3.jpg)

## Video
  [Watch the Video here](https://drive.google.com/file/d/1XEF7YHhLAk87abD7xn3mRei2RSw-zyJc/view?usp=sharing)

## Implementation
As mentioned earlier I have used google places API and google directions API, the drop down that suggests places as we type is Autocomplete Support Fragment which works with the API
to suggest places. I have used directions API to get the routes between the given locations and then I plot them on the map.

## Codeflow
- Initially the app asks the user for location permissions which I have requested in `onMapReady()` function. I have defined two other functions `requestLocationPermission()`
which checks if the permission is granted and if not requests user for permisssion after that I call `getDeviceLocation()` this accesses device location and updates
the map camera accordingly to that position.
- There are two autocomplete support fargments that I have used named `source` and `destination` where are basically `Where from` and `Where to` search bars and on
their onclick I have written listeners that get access to latitudes and longitudes of the selected place from the suggestions. From there I access coordinates of
source and destination and call Directions API using those coordinates.
- The directions API then returns the JSON data which contains complete directions and the route details, which I extract from the JSON and plot on the map using
Polyline , by decoding polyline data from JSON. Then I draw it on the map on the click of show route button. Further I also extract duration and distance from the JSON and 
update TextView accordingly.
