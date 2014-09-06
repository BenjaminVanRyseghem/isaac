# Isaac

[HipChat][1] dedicated robot

[1]: https://www.hipchat.com
## Prerequisites

You will need [Leiningen][2] 1.7.0 or above installed.

[2]: https://github.com/technomancy/leiningen

## Installation

Once your server is deployed and running,
go in your online HipChat **Group admin** page.

Go to *Integrations*, then click *Build and install your own integration*.
You should then fill up the *Integration URL* field with the URL of your server.

**The Integration URL must be the same that the "callback-url" from your `info.json` file**

## Running

To start a web server for the application, run:

    $ lein ring server-headless [port]

## Compiling

To compile the application as a stand-alone deplaoyable server, run:

    $ lein ring uberjar

The generated jars can be found in `./target`

You can then run it using:

    $ java -jar isaac-0.1.0-standalone.jar

Note that an `info.json` file is needed along the `jar` file.

## info.json structure

The `info.json` file is structured like this: 

		{
		  "url" : String -- URL of your HipChat lobby (e.g. https://my-company.hipchat.com),
		  "callback-url" : String -- URL where your deployed server is reacheable (e.g. http://www.my-server.com:8888)
		}

## License

Copyright © 2014 Benjamin Van Ryseghem.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
See `LICENCE` for me information.