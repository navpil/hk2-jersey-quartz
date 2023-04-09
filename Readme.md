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

Code was taken and adapted from [StackOverflow](https://stackoverflow.com/questions/42951949/hk2-factory-for-quartz-jobs-not-destroying-service-after-execution)

To check how the BookService can be accessed outside the scope of BookApplication, call the:

    http://localhost:8080/quartzhk2/nonmanaged/bookshack

and check `NonManagedOldSchoolServlet`

## Getting HK2 beans from non managed scope

This one is not really related to the HK2/Jersey/Quartz, but it was interesting for me how to get hold of managed beans
outside the HK2 scope.
Why would I need it?
Some legacy applications can still be written with servlets and gradually migrated to Jersey.
In this case accessing some HK2-managed beans is very desirable.

Getting hold of the injected resource outside HK2 context is not that easy.
I demonstrate how to do it with the hack of having a static helper.
In this way static context plays the role of an Uber Context, since it's managed by JVM.
Since I dislike storing dynamic data in static fields I called this approach a _Horrible Hack_.

## Eager Initialization

To eagerly initialize something, in our case - starting a Quartz job one can use several approaches

 - Immediate component
 - Immediate resource
 - ContainerLifecycleListener

### Immediate component/resource

The cleanest way is definitely an _Immediate component_.
Inside the binder, bind your component with an `@Immediate` scope 
(please note that putting this annotation directly on class did not work).

    bindAsContract(BooksQuartzJobExecutor.class).in(Immediate.class);

In order this to work register `io.github.navpil.q.common.ImmediateFeature.class`.
And note the `@PostConstruct` annotation on `BooksQuartzJobExecutor` where the job is actually started.

Annotation `@Immediate` work on resources (such as `InitializeUberContextResource`).
You may inject classes there to do some actions on startup, but scoping beans with `Immediate.class` is cleaner.

### ContainerLifecycleListener

Register an implementation of a `ContainerLifecycleListener` and in the `onStartup` method you can do:

    ServiceLocator serviceLocator = container.getApplicationHandler()
            .getInjectionManager()
            .getInstance(ServiceLocator.class);

And then do whatever you want, for example:

    serviceLocator.getService(BooksQuartzJobExecutor.class).start()

or

    UberContextHorribleHack.putServiceLocator(BooksApplication.NAME, serviceLocator);

Demonstrated with `BooksApplicationContainerLifecycleListener`

## Some links

 - [Basic HK2 tutorial](https://riptutorial.com/jersey/example/23632/basic-dependency-injection-using-jersey-s-hk2)
 - [Jersey/HK2 with web.xml](https://www.appsdeveloperblog.com/dependency-injection-hk2-jersey-jax-rs/)
