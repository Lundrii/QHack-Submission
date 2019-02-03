# CIRCUIT SAFE - QHACKS HACKATHON 2019

Circuit safe is a PCB design validator. This repo is an android app front-end for the node.js backend. It allows for the selection of a gerber file alongside with basic parameters such as the minimum width between tracers. The file and parameters are sent to the backend via http post request. The server sends back a rendered image of the PCB, as well as calculations for the circuit's viability, such as the maximum through-put current that can be given through a given trace.

[screenshot](https://imgur.com/WuQoXq8)

[quick demo video](https://youtu.be/7CH2nNuY6H0)
