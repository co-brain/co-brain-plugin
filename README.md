## Inspiration
Every coder in a team knows the problem. One reads code someone else wrote, the documentation is bad so you have to waste your time trying to understand what the code actually does or waste time searching for the author. Our plugin aims to solve this problem.

## What it does
Our software helps you as a developer to save time, code more efficiently and concentrate on problem solving.
It does so by providing you with an easy way to contact your fellow teammates, when trying to understand the code they authored. Without leaving the IDE you are able to select the respective code and request further explanation or better documentation. In the background the responsible author is determined and a message with an absolute reference to the corresponding code chunk is being sent. Beside the time saving it also prevents misunderstandings between you and your colleagues.

## How we built it
Since the purpose of the whole idea is to not leave the IDE we started with implementing an Intellij Plugin.
For that we used Java. Additionally we implemented the management of versioning related functionality in python for rapid development of a prototype. In the versioning section we heavily rely on Git since its state of the art. Since there is a variety of messaging services used by different organisations/teams we offer a generic interface. You can easily add services like Teams, Slack or Mail (which we implemented for demonstration purposes).

## Challenges we ran into
- Gradle was driving us nuts
- Git did not offer all the functionality we needed so we had to adapt
- The Outlook Rest-API is a fucking mess
- We first developed in Java and Python but they did not like each other

## Accomplishments that we're proud of
- Providing a robust but flexible workflow might enrich the lives of thousands of developers :)
- Successfully integrating a plugin into Intellij!!1!

## What we learned
- Getting Gradle up and running (had no experience before)
- Nobody really knows git

## What's next for co-brain
In the future want to improve the workday of coders further. This means improving/extending the proposed solution. Besides we have one more major idea:
### Integration of Stackoverflow
Like in the proposed solution we want to enable coders to be able to retrieve answers from Stackoverflow without leaving the IDE.

## And last but not least
### 2020: Intellij.sexy
