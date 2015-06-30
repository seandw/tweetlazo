# tweetlazo

![Example of tweetlazo](/example.png "An example of tweetlazo used to track 2/3 of a Chile/Peru match ending 2 - 1")

tweetlazo is an akka-powered program that watches provided Twitter
hashtags for activity. Since it is soccer-centric, its primary concern
is with identifying tweets that have something to do with goals.

## Setup

In its current state, tweetlazo uses JavaFX 8 for its GUI, so be sure
to have JDK 8u40 or higher installed.

tweetlazo takes advantage of Twitter's Streaming API, which requires
the user to register for and use OAuth credentials. tweetlazo uses the
`twitter4j` library, which provides
[some different ways][twitter4j-conf] to provide those credentials to
the program. The easiest option would be to add a
`twitter4j.properties` with your credentials to `src/main/resources/`.

## Running

Running tweetlazo is straightforward. tweetlazo can take an arbitrary
number of hashtags (though really, you might want to stick with one or
two) to track. Do **not** include the `#` in your hashtags, those are
implied.

    $ sbt "run hashtag hashtag ..."

## TODO

tweetlazo was originally made to see how Twitter users react to goals
in soccer games. While the GUI only shows tweet volume and categorizes
tweets in only non-goal and goal buckets, the system collects other
information (like how many extra letters are in users' goal
celebrations). In the future, it would be nice for that information to
be exposed to the GUI as well, but also to track other events.

Oh, and more tests. Always more tests.

## License

```
The MIT License (MIT)

Copyright (c) 2015 Sean Webster

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

[twitter4j-conf]: http://twitter4j.org/en/configuration.html
