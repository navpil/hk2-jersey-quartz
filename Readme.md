# HK2 + Jersey + Quartz on Tomcat sample project

 - Quartz is a scheduler
 - Jersey is a REST implementation
 - HK2 is Jersey native DI framework

Problem: how to create Quartz Jobs managed by HK2?

## Solution

This project injects Quartz into the HK2 and thus can create HK2 aware jobs.

Package an application and deploy on Tomcat.

Call 

    http://localhost:8080/quartzhk2/booksapp/books/

And check that the time is updated with scheduler.

Most of the interesting stuff is written in `BooksApplication`
