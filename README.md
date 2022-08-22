TO-DO Notes - Code for 5.1-5.3 Testing Codelab
============================================================================

Code for the Advanced Android Kotlin Testing Codelab 5.1-5.3

Introduction
------------

Firstly it was this codelab series with ServiceLocator without DI(Hilt)
So firest i changed almost everything that found confusing, then continued codelabs after codelabs.
I was trying to write tests only looking at tests names and tried to understand the code
After some time it just became my playground where i test all new stuff that i learned 

Here will be some remarks
(Never use fragment factory, DataBinding can be easily replaced just with viewBinding idk about JetPackCompose yet,
Custom views is difficult to test, Migration to coroutine 1.6 was exciting,
@Binds > @Provides but more difficult to use,
private val viewModel: ItemsViewModel by viewModels() with hilt is OP because 
most parameters saved from previous fragment you could access through SavedStateHandle,
Robolectric is ABOMINATION but i lack of understanding of android not to use it,
Instead of TDD i can just write my code in a way that always be testable to avoid problems,
i wanna learn ktor, continue to code)
By far it's the most important remarks after a long month




TO-DO Notes is an app where you to write down tasks to complete. The app displays them in a list.
You can then mark them as completed or not, filter them and delete them.

![App main screen, screenshot](screenshot.png)

This codelab has four branches, representing different code states:

* [starter_code](https://github.com/googlecodelabs/android-testing/tree/starter_code)
* [end_codelab_1](https://github.com/googlecodelabs/android-testing/tree/end_codelab_1)
* [end_codelab_2](https://github.com/googlecodelabs/android-testing/tree/end_codelab_2)
* [end_codelab_3](https://github.com/googlecodelabs/android-testing/tree/end_codelab_3)

The codelabs in this series are:
* [Testing Basics](https://codelabs.developers.google.com/codelabs/advanced-android-kotlin-training-testing-basics)
* [Introduction to Test Doubles and Dependency Injection](https://codelabs.developers.google.com/codelabs/advanced-android-kotlin-training-testing-test-doubles)
* [Survey of Testing Topics](https://codelabs.developers.google.com/codelabs/advanced-android-kotlin-training-testing-survey)


Pre-requisites
--------------

You should be familiar with:

* The Kotlin programming language, including [Kotlin coroutines](https://developer.android.com/kotlin/coroutines) and their interaction with [Android Jetpack components](https://developer.android.com/topic/libraries/architecture/coroutines).
* The following core Android Jetpack libraries: [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel),
 [LiveData](https://developer.android.com/topic/libraries/architecture/livedata),
  [Navigation Component](https://developer.android.com/guide/navigation) and 
  [Data Binding](https://developer.android.com/topic/libraries/data-binding).
* Application architecture, following the pattern from the [Guide to app architecture](https://developer.android.com/jetpack/docs/guide) and [Android Fundamentals codelabs](https://developer.android.com/courses/kotlin-android-fundamentals/toc).


Getting Started
---------------

1. Download and run the app.
2. Check out one of the codelabs mentioned above.

License
-------

Copyright 2019 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
