# ChangePropagation

## Overview
Software graphs, mutation testing and propagation estimer tools.
This framework is split into three tools.
Those tools are used for my PhD research purposes.

## Prerequisites

Please install Java and Maven being able to run those tools.
Moreover, install git or svn in order to obtain projects *with tests*.

### Simple Mutation Framework (smf)
Tool performing Java Software Mutations. This software produces new versions of a software, based on specific mutations operator.
Mutation operators are defined (extendable easily with new ones).

### Software Miner (softminer)
Tool used for producing Java Software graphs. Currently support Call Graphs including or not:
- Class Hierarchy Analysis (CHA);
- Global fields access;

It proposes a total of four types of different graphs. It is based on the Spoon library for producing exploring the code.

### Propagation Miner (pminer)

Tool used to compute some statistics regarding the produces mutants and the software graphs produced with smf and softminer.
Including: precisions, recall and f-scores.

## Research Papers

This framework is used in the following papers:

- __Vincenzo Musco__, Martin Monperrus, Philippe Preux. An Experimental Protocol for Analyzing the Accuracy of Software Error Impact Analysis. Tenth IEEE/ACM International Workshop on Automation of Software Test, May 2015, Florence, Italy.

## How to use

All commands can take the ``-h`` or ``--help`` option which will give the list of possible options for the command.

### Create project, mutants and run tests

Checkout ChangePropagation and package with maven:

```
$ git checkout https://github.com/v-m/ChangePropagation.git
$ mvn package
```

(optional) Create eclipse projects:

```
$ mvn eclipse:eclipse
```

In a new terminal, checkout a project and switch to the desired version (here we consider the Apache Common Lang rev. 6965455):

```
$ cd /tmp
$ git clone http://git-wip-us.apache.org/repos/asf/commons-lang.git
$ cd commons-lang
$ git reset --hard 6965455
```

Create a new project

```
$ ./smf-newproject /tmp/myproject /tmp/commons-lang/
```

Build and test the project

```
$ ./smf-newproject /tmp/myproject
```

Choose and create a mutation for a specific operator:

```
$ ./smf-createmutation -o
$ ./smf-createmutation /tmp/myproject ABS
```

Run tests on mutants:

```
$ ./smf-runmutants /tmp/myproject/mutations/main/ABS/
```

### Generate graphs

This tool can simply use projects created with smf to generate graphs from it. To do so, just invoke:

```
$ ./softminer-creategraph /tmp/myproject/
```

To generate other type of graphs and more options, just add the ``--help`` parameter to the program.

### Use JavaPDG as a graph data source

- Extract a JavaPDG
- Use the ``pminer-javapdg`` command.

*(more details soon...)*

### Obtain propagation statistics

Let consider the following hierarchy:

- mut_result
 - soft1
   - mutations
     - main
        - ABS
          - mutations.xml
          - exec
            - mutant_52.xml
            - mutant_1045.xml
            - (...)
        - AOR
        - (...)
   - smf.run.xml
   - graph.xml
   - graph_cha.xml
 - soft2
   - (...)

#### Detailed performances for one project/mutation operator

To get the performances of mutation using the CHA graph for soft1 software and the ABS mutation operator:

```
./pminer-mutop-perf mut_result/soft1/graph_cha.xml mut_result/soft1/mutations/main/ABS/mutations.xml
```

Following options are availables:

- ``-k``: include only killed mutants in the analysis;
- ``-r``: remove nulls from the medians calculation;
- ``-n <nb>``: filter out if more than <nb> mutants are present;
- ``-c <sep>``: export in csv format with <sep> separator.

#### Global performances for all projects mutation operators

To get the performances of mutation with and without CHA graph for all soft present in mut_result and all mutation operator:

```
./pminer-global-perf mut_result graph.xml:graph_cha.xml
```

If there is only the two softwares listed above the following command is equivalent:

```
./pminer-global-perf mut_result/soft1:mut_result/soft2 graph.xml:graph_cha.xml
```

Similar options than for ``pminer-mutop-perf`` can be used. Moreover, the ``-a`` options allows to compute averages instead of medians.

## Dependencies

 - junit
 - commons-cli
 - commons-io
 - jdom 2
 - log4j
 - spoon


## Contact

See: http://www.vmusco.com or http://www.vincenzomusco.com