# Programing languages statistics scraper

[Java tool](https://github.com/C0deboy/jjp-stats-scraper) rewrited to Kotlin which collects (scraps from web) statistics for programing languages for my site [jaki-jezyk-programowania.pl](https://jaki-jezyk-programowania.pl/).

Currently, the tool is fetching data for each language from:
- [Github](https://github.com/)
    * top 10 projects
    * number of projects
    * number of projects with more than 1000 stars
 - [Meetup.com](https://www.meetup.com/pl-PL/topics/JavaScript)
    * number of members
    * number of meetups
 - [StackOverflow](https://stackoverflow.com)
     * number of tagged questions
 - [Wikipedia](https://en.wikipedia.org/wiki/Main_Page)
    * latest language version
 - [Tiobe INDEX](https://www.tiobe.com/tiobe-index/)
    * position at last year
    * position at this year
 - [Spectrum ranking](https://spectrum.ieee.org/static/interactive-the-top-programming-languages-2017)
    * position at last year
    * position at this year
 
 Everything is stored in two json files:
    - statistics.json
    - languagesVersions.json
    
 If statistics.json already exists, then it will be renamed (date will be appended) and fresh one will be created as statistics.json.


### Running

Note:
```
If you will run this tool more than once within short time then errors occurs due to Github api restrictions. 
```

Provide github authentication token under `src\main\resources\config.properties` if u want to fetch data from Github

Follow this [guide](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) if u don't have token.

```
GithubAuthToken=token 22sadasdsa34r32412342134214324123
```

Otherwise, you need to pass parameter remove line where `GithubDataScraper` is added at `Main` class.

Sample log output of the tool:

![Sample output of tool](demo/sample.png)
![Sample output of tool 2](demo/sample2.png)

## Built With

* [Kotlin](https://kotlinlang.org/) - Concise programming language
* [Gradle](https://maven.apache.org/) - Dependency management and build tool.
* [Jsoup](https://jsoup.org/) - Used to parse HTML websites.
* [Klaxon](https://github.com/cbeust/klaxon) - JSON parser
* [Apache Commons](https://commons.apache.org/) - Helper libs for validating data.
* [Logback](https://commons.apache.org/) - Status logging.
* [Junit 5](https://junit.org/junit5/) - Unit tests
* [Mockk](https://github.com/mockk/mockk) - Mocking library for Kotlin
* [AssertJ](http://joel-costigliola.github.io/assertj/) - Fluent assertions for Java