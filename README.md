# LAB3_Besturingssystemen2
Github: https://github.com/brechtvandesijpe/LAB3_Besturingssystemen2
---

## Allocator
De Allocator package bevat volgende onderdelen:
- Allocator interface
- Foute voorbeeldimplementatie van Allocator genaamd MyAllocatorImpl
- Backingstore, om map en unmap calls te doen naar het OS
- MTAllocator als multithreaded implementatie van Allocator, bevat een map van STAllocators
- STAllocator als singlethreaded implementatie van Allocator, bevat een map van Arenas
- Arena een verzameling van geheugenblokken
- Block als voorstelling van een geheugenblok in een arena
- Allocator-, Arena- en BlockException klassen dienen om exception-handling te voorzien binnen de implementatie van Block, Arena en Allocator


## Debugger
De debugger package bevat volgende onderdelen:
- Aparte debuggerklassen voor de klassen Block, Arena, STAllocator en Allocator
- Een Main klasse die alle debuggers oproept en voor de output zorgt
- Een logger die berichten naar de console logt met een timestamp - threadId van de uitvoerende thread en een stacktrace naar waar de logmethode werd opgeroepen, door extra diepte mee te geven in de logmethode kan de stacktrace meer of minder ver weergegeven worden
- Een klasse StackTracer om de stack weer te geven in de logger
- Workerklassen om eenzelfde Block, Arena of Allocator tegelijk te gebruiken om zo de synchronisatie te testen (niet standaard in Main verwerkt)
- TesterFailed- en TesterSuccessException klassen dienen om de exception-handling te voorzien binnen de debugger

## BankingSimulation
Bevat de testcase voor de allocator.