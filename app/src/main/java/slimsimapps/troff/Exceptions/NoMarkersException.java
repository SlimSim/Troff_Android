package slimsimapps.troff.Exceptions;

//TODO: change RuntimeException -> Exception
// - this will force me to always catch this exception :)
public class NoMarkersException extends RuntimeException {

String message;

public NoMarkersException(String s) {
	super(s);

	//	this.cause = cause;
	this.message = s;

}
}
