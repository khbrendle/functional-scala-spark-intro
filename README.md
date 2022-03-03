# Functional Scala Intro

This project provides a basic setup for a functional Spark Scala application.

this project is using Spark 3.1.2, if you want to use a different

# Building
to build the executable program run this command
```{shel}
sbt compile assembly
```
this will create the jar file that we can then run against spark with
```{shell}
spark-submit --master="local[2]" --deploy-mode=client target/scala-2.12/fss-0.1.0.jar
```

## Useful links
- [related slides](https://docs.google.com/presentation/d/1R7rQyms_aGHokzAYuRImUNkubhc1_JQPDv2gD35ttxg/edit?usp=sharing)
- [Pagila test data](https://github.com/devrimgunduz/pagila)