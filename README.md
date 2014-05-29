bamLogClient
===========================

This is a client that is used send log entries of local log files to an online BAM server in real time.

Configure logClient.conf like the example and start the client

when logs are written to the log files specified in the conf this utility will publish them to bam

This will create a columnfamily with name logs_ip_separated_by_underscore in BAM cassandra database

These published logs can be viewed by Installing bamlogviewer toolbox to BAM Server


bamlogviewer toolbox - https://github.com/mihilranathunga/BamLogViewer
bamlogviewer toolbox BAM dependencies - https://github.com/mihilranathunga/BamLogViewerCassandraClient

 

