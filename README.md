# SCLFG - Star Citizen : Looking For Group

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](#) [![Platform](https://img.shields.io/badge/Language-Kotlin-yellowgreen.svg)](#)

#### Starting a :family: LFG android app for the sci-fi game :star2: Star Citizen.

This app is meant to serve as a tool for the Star Citizen and fill a gap in the ability for users to connect and play together. The app requires email, password authentication currently. Authentication is handled with Google's :fire: FireStore cloud database solution.

You can now click the bottom right of group listings to expand the player list view. If you are not in a group you have the ability to join a new group. You can now leave a group as well. :sparkles:

You can set groups that you own to private to not appear in the public group listings. Public Group Listings update on any change to the groups list in the database live. Click on the floating action button in the expanded group to access the group messaging view. :fire: :fire:

#### Using the App!

The app is broken into three main screens: Create, List, and Me. The Me screen contains all of the groups you are in as well as privacy toggles for any groups that you are the owner of. Clicking a group card under My Groups will take you to the Group Screen featuring group messaging and an About Group section. For any groups you own, you also have the option to swipe the cell to delete. 

In the list screen you can see all public group listings. Click the person to join a group and the X to leave. The white button will expand the group cell to show existing users of the particular group. After joining a group a button will show in the sub list to view the dedicated group screen. 

Finally, in the Create screen, you can create new groups which you will be added to automatically on creation.

From any of the main screens, you can access the Sign Out and Change screen name options by clicking the three dots menu in the upper right. 

<p align="center">
  <img width="420" src="https://reduxkotlin.org/img/redux-logo-landscape.png">
</p>

Recently I transitioned the state management from ViewModels to Redux. The result is a more robust system that allows for a better debugging experience. Though this benefit does come at a cost. Theres bound to be some state related bugs floating around. Everything appears good on the surface but let me know if anything odd happens...

#### Known Issues

- If in a dedicated group screen and the group is delete by another user. The app will crash...

- Tool bar items for sign out and change screen name should appear as icons. Not only in the overflow menu (three dots menu).


## Preview

|                                       Login                                        |                                       Register                                        |
| :--------------------------------------------------------------------------------: | :-----------------------------------------------------------------------------------: |
| <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgLogin.png" > | <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgRegister.png" > |

|                                   Group Creation                                    |                                   Group Listing                                   |
| :---------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------: |
| <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgSearch.png" > | <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgList.png" > |

|                                     Messaging View                                     |                                    Group About                                     |
| :------------------------------------------------------------------------------------: | :--------------------------------------------------------------------------------: |
| <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgMessaging.png" > | <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgAbout.png" > |

|                                       Profile                                        |                                       AutoComplete                                        |
| :----------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------: |
| <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgProfile.png" > | <img src="https://github.com/Cougargriff/SCLFG/blob/master/.images/lfgAutoComplete.png" > |

