[![Build Status](https://travis-ci.org/sirixdb/sirix.png)](https://travis-ci.org/sirixdb/sirix)
[![All Contributors](https://img.shields.io/badge/all_contributors-23-orange.svg?style=flat-square)](#contributors)
[![CodeFactor](https://www.codefactor.io/repository/github/sirixdb/sirix/badge)](https://www.codefactor.io/repository/github/sirixdb/sirix)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Maven Central](https://img.shields.io/maven-central/v/io.sirix/sirix-core.svg)](https://search.maven.org/search?q=g:io.sirix)
[![Coverage Status](https://coveralls.io/repos/github/sirixdb/sirix/badge.svg?branch=master)](https://coveralls.io/github/sirixdb/sirix?branch=master)

<p align="center"><img src="https://raw.githubusercontent.com/sirixdb/sirix/master/logo.png"/></p>

[![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=SirixDB+-+a+storage+system%2C+which+creates+%28very+small-sized%29+snapshots+of+your+data+on+every+transaction-commit+through+the+implementation+of+a+novel+sliding+snapshot+algorithm.&url=http://sirix.io&via=sirix&hashtags=versioning,diffing,xml,kotlin,coroutines,vertx)

[![Follow](https://img.shields.io/twitter/follow/sirixdb.svg?style=social)](https://twitter.com/sirixdb)

[Download ZIP](https://github.com/sirixdb/sirix/archive/master.zip) | [Join us on Slack](https://join.slack.com/t/sirixdb/shared_invite/enQtNjI1Mzg4NTY4ODUzLTE3NmRhMWRiNWEzMjQ0NjAxNTZlODBhMTQzMWM2Nzc5MThkMjlmMzI0ODRlNGE0ZDgxNDcyODhlZDRhYjM2N2U) | [Community Forum](https://sirix.discourse.group/)

**Working on your first Pull Request?** You can learn how from this *free* series [How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github) and another tutorial: [How YOU can contribute to OSS, a beginners guide](https://dev.to/itnext/how-you-can-contribute-to-oss-36id)

<h1 align="center">SirixDB - An Evolutionary, Temporal NoSQL Document Store</h1>
<h2 align="center">Store and query revisions of your data efficiently</h2>

>"Remember that you're lucky, even if you don't think you are, because there's always something that you can be thankful for." - Esther Grace Earl (http://tswgo.org)

**We currently support the storage and (time travel) querying of both XML - and JSON-data in our binary encoding which is tailored to support versioning. Our index-structures and the whole storage engine has been written from scratch to support versioning natively. In the future, we might also support the storage and querying of other data formats.**

<p>&nbsp;</p>

<p align="center"><img src="https://raw.githubusercontent.com/sirixdb/sirix/master/showcase/screencast-three-revisions-faster.gif"/></p>

<p>&nbsp;</p>

**Note: Work on a [Front-end](https://github.com/sirixdb/sirix-svelte-front-end) built with [Svelte](https://svelte.dev), [D3.js](https://d3js), and Typescript has just begun**

**Discuss it in the [Community Forum](https://sirix.discourse.group)**

## Table of contents
-   [Keeping All Versions of Your Data By Sharing Structure](#keeping-all-versions-of-your-data-by-sharing-structure)
-   [SirixDB Features](#sirixdb-features)
-   [Getting Started](#getting-started)
    -   [Download ZIP or Git Clone](#download-zip-or-git-clone)
    -   [Maven Artifacts](#maven-artifacts)
    -   [Docker Images](#docker-images)
    -   [Command line tool](#command-line-tool)
    -   [First steps](#first-steps)
    -   [Documentation](#documentation)
-   [RESTful-API](#restful-api)
-   [DOM alike API](#dom-alike-api) 💪
-   [Simple XQuery Examples](#simple-xquery-examples)
-   [Getting Help](#getting-help)
    -   [Community Forum](#community-forum)
    -   [Join us on Slack](#join-us-on-slack)
-   [Visualizations](#visualizations)
-   [Why should you even bother?](#why-should-you-even-bother)
-   [Features in a nutshell](#features-in-a-nutshell)
-   [Developers](#developers)
-   [Further information](#further-information)
-   [License](#license)
-   [Involved People](#involved-people)

## Keeping All Versions of Your Data By Sharing Structure
We could write quite a bunch of stuff, why it's oftentimes of great value to keep all states of your data in a storage system, but recently we stumbled across an excellent [blog post](https://www.hadoop360.datasciencecentral.com/blog/temporal-databases-why-you-should-care-and-how-to-get-started-par), which explains the advantages of keeping historical data very well. In a nutshell, it's all about looking at the evolution of your data, finding trends, doing audits, implementing efficient undo-/redo-operations... the [Wikipedia page](https://en.wikipedia.org/wiki/Temporal_database) has a bunch of examples. We recently also added use cases over [here](https://sirix.io/documentation.html).

Our strong belief is that a temporal storage system must address the issues, which arise from keeping past states way better than traditional approaches. Usually, storing time-varying, temporal data in database systems, which do not support the storage thereof natively results in a lot of unwanted hurdles. Storage space is wasted, query performance to retrieve past states of your data is not ideal and usually temporal operations are missing altogether.

Data must be stored in a way, that storage space is used as effectively as possible while supporting the reconstruction of each revision, as it was seen by the database during the commits, in linear time, no matter if it's the very first revision or the most recent revision. Ideally, query time of old/past revisions, as well as the most recent revision should be in the same runtime complexity (logarithmic when querying for specific records).

We not only support snapshot-based versioning on a record granular level through a novel versioning algorithm called sliding snapshot, but also time travel queries, efficient diffing between revisions and the storage of semi-structured data to name a few.

The following time-travel query to be executed on our binary JSON representation of [Twitter sample data](https://raw.githubusercontent.com/sirixdb/sirix/master/bundles/sirix-core/src/test/resources/json/twitter.json) gives an initial impression of what's possible:

```xquery
let $statuses := jn:open('mycol.jn','mydoc.jn', xs:dateTime('2019-04-13T16:24:27Z'))=>statuses
let $foundStatus := for $status in bit:array-values($statuses)
  let $dateTimeCreated := xs:dateTime($status=>created_at)
  where $dateTimeCreated > xs:dateTime("2018-02-01T00:00:00") and not(exists(jn:previous($status)))
  order by $dateTimeCreated
  return $status
return {"revision": sdb:revision($foundStatus), $foundStatus{text}}
```

The query opens a database/resource in a specific revision based on a timestamp (`2019–04–13T16:24:27Z`) and searches for all statuses, which have a `created_at` timestamp, which has to be greater than the 1st of February in 2018 and did not exist in the previous revision. `=>` is a dereferencing operator used to dereference keys in JSON objects, array values can be accessed as shown with the function bit:array-values or through specifying an index, starting with zero: array[[0]] for instance specifies the first value of the array.

## SirixDB Features
SirixDB is a log-structured, temporal NoSQL document store, which stores evolutionary data. It never overwrites any data on-disk. Thus, we're able to restore and query the full revision history of a resource in the database.

### Design Goals
Some of the most important core principles and design goals are:

<dl>
  <dt>Embeddable</dt>
  <dd>Similar to SQLite and DucksDB SirixDB is embeddable at its core. Other APIs as the non-blocking REST-API are built on top.</dd>
  <dt>Minimize Storage Overhead</dt>
  <dd>SirixDB shares unchanged data pages as well as records between revisions, depending on a chosen versioning algorithm during the initial bootstrapping of a resource. SirixDB aims to balance read and writer performance in its default configuration.</dd>
  <dt>Concurrent</dt>
  <dd>SirixDB contains very few locks and aims to be as suitable for multithreaded systems as possible.</dd>
  <dt>Asynchronous</dt>
  <dd>Operations can happen independently; each transaction is bound to a specific revision and only one read/write-transaction on a resource is permitted concurrently to N read-only-transactions.</dd>
  <dt>Versioning/Revision history</dt>
  <dd>SirixDB stores a revision history of every resource in the database without imposing extra overhead. It uses a huge persistent, durable page-tree for indexing revisions and data.</dd>
  <dt>Data integrity</dt>
  <dd>SirixDB, like ZFS, stores full checksums of the pages in the parent pages. That means that almost all data corruption can be detected upon reading in the future, we aim to partition and replicate databases in the future.</dd>
  <dt>Copy-on-write semantics</dt>
  <dd>Similarly to the file systems Btrfs and ZFS, SirixDB uses CoW semantics, meaning that SirixDB never overwrites data. Instead, database-page fragments are copied/written to a new location.</dd>
  <dt>Per revision and page versioning</dt>
  <dd>SirixDB does not only version on a per revision, but also on a per page-base. Thus, whenever we change a potentially small fraction
of records in a data-page, it does not have to copy the whole page and write it to a new location on a disk or flash drive. Instead, we can specify one of several versioning strategies known from backup systems or a novel sliding snapshot algorithm during the creation of a database resource. The versioning-type we specify is used by SirixDB to version data-pages.</dd>
  <dt>Guaranteed atomicity and consistency (without a WAL)</dt>
  <dd>The system will never enter an inconsistent state (unless there is hardware failure), meaning that unexpected power-off won't ever damage the system. This is accomplished without the overhead of a write-ahead-log. (<a
href="https://en.wikipedia.org/wiki/Write-ahead_logging">WAL</a>)</dd>
  <dt>Log-structured and SSD friendly</dt>
  <dd>SirixDB batches writes and syncs everything sequentially to a flash drive
during commits. It never overwrites committed data.</dd>
</dl>

## Revision Histories
**Keeping the revision history is one of the main features in
SirixDB.** We're able to revert any revision into an earlier
version or back up the system automatically without the overhead of
copying. SirixDB only ever copies changed database-pages and depending
on the versioning algorithm we chose during the creation of a
database/resource only page-fragments, as well as ancestor index-pages
to create a new revision.

We can reconstruct every revision in <em>O(n)</em>, where <em>n</em>
denotes the number of nodes in the revision. Binary search is used on
an in-memory (linked) map to load the revision, thus finding the
revision root page has an asymptotic runtime complexity of <em>O(log
n)</em>, where <em>n</em>, in this case, is the number of stored
revisions.

Currently, SirixDB offers two built-in native data models, namely a
binary XML store as well as a JSON store.

<p>&nbsp;&nbsp;</p>

<p align="center"><img src="https://github.com/JohannesLichtenberger/sirix/raw/master/bundles/sirix-gui/src/main/resources/images/sunburstview-cut.png"/></p>

<p>&nbsp;&nbsp;</p>

Articles published on Medium:
- [Asynchronous, Temporal  REST With Vert.x, Keycloak and Kotlin Coroutines](https://medium.com/hackernoon/asynchronous-temporal-rest-with-vert-x-keycloak-and-kotlin-coroutines-217b25756314)
- [Pushing Database Versioning to Its Limits by Means of a Novel Sliding Snapshot Algorithm and Efficient Time Travel Queries](https://medium.com/sirixdb-sirix-io-how-we-built-a-novel-temporal/why-and-how-we-built-a-temporal-database-system-called-sirixdb-open-source-from-scratch-a7446f56f201)
- [How we built an asynchronous, temporal RESTful API based on Vert.x, Keycloak and Kotlin/Coroutines for Sirix.io (Open Source)](https://medium.com/sirixdb-sirix-io-how-we-built-a-novel-temporal/how-we-built-an-asynchronous-temporal-restful-api-based-on-vert-x-4570f681a3)
- [Why Copy-on-Write Semantics and Node-Level-Versioning are Key to Efficient Snapshots](https://hackernoon.com/sirix-io-why-copy-on-write-semantics-and-node-level-versioning-are-key-to-efficient-snapshots-754ba834d3bb)

## Getting started

### [Download ZIP](https://github.com/sirixdb/sirix/archive/master.zip) or Git Clone

```
git clone https://github.com/sirixdb/sirix.git
```

or use the following dependencies in your Maven (or Gradle?) project.

We just changed to Java14 (OpenJDK 14).

### Maven artifacts
At this stage of development, you should use the latest SNAPSHOT artifacts from [the OSS snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/io/sirix/) to get the most recent changes.

Just add the following repository section to your POM or build.gradle file:
```xml
<repository>
  <id>sonatype-nexus-snapshots</id>
  <name>Sonatype Nexus Snapshots</name>
  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```
```groovy
repository {
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots/"
        mavenContent {
            snapshotsOnly()
        }
    }
}
```

<strong>Note that we changed the groupId from `com.github.sirixdb.sirix` to `io.sirix`. Most recent version is 0.9.6-SNAPSHOT.</strong>

Maven artifacts are deployed to the central maven repository (however please use the SNAPSHOT-variants as of now). Currently, the following artifacts are available:

Core project:
```xml
<dependency>
  <groupId>io.sirix</groupId>
  <artifactId>sirix-core</artifactId>
  <version>0.9.6-SNAPSHOT</version>
</dependency>
```
```groovy
compile group:'io.sirix', name:'sirix-core', version:'0.9.6-SNAPSHOT'
```

Brackit binding:
```xml
<dependency>
  <groupId>io.sirix</groupId>
  <artifactId>sirix-xquery</artifactId>
  <version>0.9.6-SNAPSHOT</version>
</dependency>
```
```groovy
compile group:'io.sirix', name:'sirix-xquery', version:'0.9.6-SNAPSHOT'
```

Asynchronous, RESTful API with Vert.x, Kotlin and Keycloak (the latter for authentication via OAuth2/OpenID-Connect):
```xml
<dependency>
  <groupId>io.sirix</groupId>
  <artifactId>sirix-rest-api</artifactId>
  <version>0.9.4-SNAPSHOT</version>
</dependency>
```

```groovy
compile group: 'io.sirix', name: 'sirix-rest-api', version: '0.9.6-SNAPSHOT'
```

Other modules are currently not available (namely the GUI, the distributed package as well as an outdated Saxon binding).

### Docker images for the Sirix HTTP(S)-Server / the REST-API
First, we need a running Keycloak server for now on port 8080.

As a Keycloak instance is needed for the RESTful-API we'll build a simple docker compose file maybe with a demo database user and some roles in the future.

For running a keycloak docker container you could for instance use the following docker command:
`docker run -d --name keycloak -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -e KEYCLOAK_LOGLEVEL=DEBUG jboss/keycloak`. Afterwards it can be configured via a Web UI: http://localhost:8080. Keycloak is needed for our RESTful, asynchronous API. It is the authorization server instance.

Docker images of Sirix can be pulled from Docker Hub (sirixdb/sirix). However the easiest way for now is to download Sirix, then

1. Change into the sirix-rest-api bundle: `cd bundles/sirix-rest-api`
2. Change the configuration in `src/main/resources/sirix-conf.json` and add the secret from Keycloak (see for instance this great [tutorial](
https://piotrminkowski.wordpress.com/2017/09/15/building-secure-apis-with-vert-x-and-oauth2/) and change the HTTP(S)-Server port Sirix is listening on:

<img src="https://piotrminkowski.files.wordpress.com/2017/09/vertx-sec-3.png"/>

3. You can simply use the example `key.pem`/`cert.pem` files in `src/main/resources` for HTTPS (for example.org), but you have to change it, once we release the stable version for production. Then you for sure have to use a certificate/key for your domain. You could use [Let's Encrypt](https://letsencrypt.org/) for instance to get an SSL/TLS certificate for free.
4. Build the docker image: `docker build -t sirixdb/sirix`
5. Run the docker container: `docker run --network=host -t -i -p 9443:9443 sirixdb/sirix` (on Windows this does not seem to work)

Sirix should be up and running afterward. Please let us know if you have any trouble setting it up.

### Command-line tool
We ship a (very) simple command-line tool for the sirix-xquery bundle:

Get the [latest sirix-xquery JAR](https://oss.sonatype.org/content/repositories/snapshots/io/sirix/sirix-xquery/) with dependencies.

### First steps
Please have a look into our sirix-example project how to use Sirix from Java or have a look into the [documentation](https://sirix.io/documentation.html).

### Documentation
We are currently working on the documentation. You may find first drafts and snippets in the [documentation](https://sirix.io/documentation.html) and in this README. Furthermore, you are kindly invited to ask any question you might have (and you likely have many questions) in the community forum (preferred) or in the Slack channel.
Please also have a look at and play with our sirix-example bundle which is available via maven or our new asynchronous RESTful API (shown next).

The following sections show different APIs to interact with Sirix.

## Getting Help

### Community Forum
If you have any questions or are considering to contribute or use Sirix, please use the [Community Forum](https://sirix.discourse.group) to ask questions. Any kind of question, may it be an API-question or enhancement proposal, questions regarding use-cases are welcome... Don't hesitate to ask questions or make suggestions for improvements. At the moment also API-related suggestions and critics are of utmost importance.

### Join us on Slack
You may find us on [Slack](https://sirixdb.slack.com) for quick questions.

## Visualizations (built on top of the cursor-based transaction API)
<p>The following diagram shows a screenshot of an interactive visualization, which depicts moves of single nodes or whole subtrees through hierarchical edge bundling.</p>

<p align="center"><img src="https://github.com/JohannesLichtenberger/sirix/raw/master/bundles/sirix-gui/src/main/resources/images/moves-cut.png"/></p>

A screencast is available depicting the SunburstView and the TextView side by side:
http://www.youtube.com/watch?v=l9CXXBkl5vI

<p>Currently, as we focused on various improvements in performance and features of the core storage system, the visualizations are a bit dated (and not working), but in the future, we aim to bring them into the web (for instance using d3) instead of providing a standalone desktop GUI.</p>

## Project Maintainer

SirixDB is maintained by

* Johannes Lichtenberger

And the Open Source Community.

## Contributors ✨

As the project was forked from a university project called Treetank, my deepest gratitude to Marc Kramis, who came up with the idea of building a versioned, secure and energy-efficient data store, which retains the history of resources of his Ph.D. Furthermore, Sebastian Graf came up with a lot of ideas and greatly improved the implementation for his Ph.D. Besides, a lot of students worked and improved the project considerably.

Thanks goes to these wonderful people, who greatly improved SirixDB lately. SirixDB couldn't exist without the help of the Open Source community:

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/yiss"><img src="https://avatars1.githubusercontent.com/u/12660796?v=4" width="100px;" alt=""/><br /><sub><b>Ilias YAHIA</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=yiss" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/BirokratskaZila"><img src="https://avatars1.githubusercontent.com/u/24469472?v=4" width="100px;" alt=""/><br /><sub><b>BirokratskaZila</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=BirokratskaZila" title="Documentation">📖</a></td>
    <td align="center"><a href="https://mrbuggysan.github.io/"><img src="https://avatars0.githubusercontent.com/u/9119360?v=4" width="100px;" alt=""/><br /><sub><b>Andrei Buiza</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=MrBuggySan" title="Code">💻</a></td>
    <td align="center"><a href="https://www.linkedin.com/in/dmytro-bondar-330804103/"><img src="https://avatars0.githubusercontent.com/u/11942950?v=4" width="100px;" alt=""/><br /><sub><b>Bondar Dmytro</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=Loniks" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/santoshkumarkannur"><img src="https://avatars3.githubusercontent.com/u/56201023?v=4" width="100px;" alt=""/><br /><sub><b>santoshkumarkannur</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=santoshkumarkannur" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/LarsEckart"><img src="https://avatars1.githubusercontent.com/u/4414802?v=4" width="100px;" alt=""/><br /><sub><b>Lars Eckart</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=LarsEckart" title="Code">💻</a></td>
    <td align="center"><a href="http://www.hackingpalace.net"><img src="https://avatars1.githubusercontent.com/u/6793260?v=4" width="100px;" alt=""/><br /><sub><b>Jayadeep K M</b></sub></a><br /><a href="#projectManagement-kmjayadeep" title="Project Management">📆</a></td>
  </tr>
  <tr>
    <td align="center"><a href="http://keithkim.org"><img src="https://avatars0.githubusercontent.com/u/318225?v=4" width="100px;" alt=""/><br /><sub><b>Keith Kim</b></sub></a><br /><a href="#design-karmakaze" title="Design">🎨</a></td>
    <td align="center"><a href="https://github.com/theodesp"><img src="https://avatars0.githubusercontent.com/u/328805?v=4" width="100px;" alt=""/><br /><sub><b>Theofanis Despoudis</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=theodesp" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/Mrexsp"><img src="https://avatars3.githubusercontent.com/u/23698645?v=4" width="100px;" alt=""/><br /><sub><b>Mario Iglesias Alarcón</b></sub></a><br /><a href="#design-Mrexsp" title="Design">🎨</a></td>
    <td align="center"><a href="https://twitter.com/_anmonteiro"><img src="https://avatars2.githubusercontent.com/u/661909?v=4" width="100px;" alt=""/><br /><sub><b>Antonio Nuno Monteiro</b></sub></a><br /><a href="#projectManagement-anmonteiro" title="Project Management">📆</a></td>
    <td align="center"><a href="http://fultonbrowne.github.io"><img src="https://avatars1.githubusercontent.com/u/50185337?v=4" width="100px;" alt=""/><br /><sub><b>Fulton Browne</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=FultonBrowne" title="Documentation">📖</a></td>
    <td align="center"><a href="https://twitter.com/felixrabe"><img src="https://avatars3.githubusercontent.com/u/400795?v=4" width="100px;" alt=""/><br /><sub><b>Felix Rabe</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=felixrabe" title="Documentation">📖</a></td>
    <td align="center"><a href="https://twitter.com/ELWillis10"><img src="https://avatars3.githubusercontent.com/u/182492?v=4" width="100px;" alt=""/><br /><sub><b>Ethan Willis</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=ethanwillis" title="Documentation">📖</a></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/bark"><img src="https://avatars1.githubusercontent.com/u/223964?v=4" width="100px;" alt=""/><br /><sub><b>Erik Axelsson</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=bark" title="Code">💻</a></td>
    <td align="center"><a href="https://se.rg.io/"><img src="https://avatars1.githubusercontent.com/u/976915?v=4" width="100px;" alt=""/><br /><sub><b>Sérgio Batista</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=batista" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/chaensel"><img src="https://avatars2.githubusercontent.com/u/2786041?v=4" width="100px;" alt=""/><br /><sub><b>chaensel</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=chaensel" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/balajiv113"><img src="https://avatars1.githubusercontent.com/u/13016475?v=4" width="100px;" alt=""/><br /><sub><b>Balaji Vijayakumar</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=balajiv113" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/FernandaCG"><img src="https://avatars3.githubusercontent.com/u/28972973?v=4" width="100px;" alt=""/><br /><sub><b>Fernanda Campos</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=FernandaCG" title="Code">💻</a></td>
    <td align="center"><a href="https://joellau.github.io/"><img src="https://avatars3.githubusercontent.com/u/29514264?v=4" width="100px;" alt=""/><br /><sub><b>Joel Lau</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=JoelLau" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/add09"><img src="https://avatars3.githubusercontent.com/u/38160880?v=4" width="100px;" alt=""/><br /><sub><b>add09</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=add09" title="Code">💻</a></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/EmilGedda"><img src="https://avatars2.githubusercontent.com/u/4695818?v=4" width="100px;" alt=""/><br /><sub><b>Emil Gedda</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=EmilGedda" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/arohlen"><img src="https://avatars1.githubusercontent.com/u/49123208?v=4" width="100px;" alt=""/><br /><sub><b>Andreas Rohlén</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=arohlen" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/marcinbieleckiLLL"><img src="https://avatars3.githubusercontent.com/u/26444765?v=4" width="100px;" alt=""/><br /><sub><b>Marcin Bielecki</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=marcinbieleckiLLL" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/ManfredNentwig"><img src="https://avatars1.githubusercontent.com/u/164948?v=4" width="100px;" alt=""/><br /><sub><b>Manfred Nentwig</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=ManfredNentwig" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/Raj-Datta-Manohar"><img src="https://avatars0.githubusercontent.com/u/25588557?v=4" width="100px;" alt=""/><br /><sub><b>Raj</b></sub></a><br /><a href="https://github.com/sirixdb/sirix/commits?author=Raj-Datta-Manohar" title="Code">💻</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

Contributions of any kind are highly welcome!

## License

This work is released under the [BSD 3-clause license](LICENSE).
