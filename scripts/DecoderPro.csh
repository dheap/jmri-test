#!/bin/csh -f
#
#  short csh script to start DecoderPro in java ($Revision: 1.8 $)
#
#  Assumes that the program is being run from the distribution directory
#

# Uncomment and use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

# 
# Change the following to match the JMRI install directly if you would 
# like to run this script without cd'ing into the install directory.
cd /usr/local/JMRI

java -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -cp .:classes:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar:lib/jython.jar apps.DecoderPro.DecoderPro


