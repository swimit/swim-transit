#Command Line
Simple command line interface that supports linking and syncing to swim lanes

##Overview
The command line interface is a simple lightweight interface that has no external dependencies 
and allows users to see what is in specified swim lanes

##Getting Started
In order to link to a certain lane, you need to specify the host, node, and lane.
```sh
% gradle run -PappArgs="['link', '[host]', '[node]', '[lane]']"
```
Syncing is almost identical; you just need to replace `link` with `sync`.
```sh
% gradle run -PappArgs="['sync', '[host]', '[node]', '[lane]']"
```
For example, you can link and sync to the list of vehicles from the tracking app
in the sf-muni agency with the following commands:
```sh
% gradle run -PappArgs="['link', 'ws://localhost:8090', '/agency/sf-muni', 'agency/vehicles']"
% gradle run -PappArgs="['sync', 'ws://localhost:8090', '/agency/sf-muni', 'agency/vehicles']"
```
