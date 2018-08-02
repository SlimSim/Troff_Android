package slimsimapps.troff.Exceptions;

//TODO: change RuntimeException -> Exception
// - this will force me to always catch this exception :)
public class NoSongsException extends RuntimeException {

String message;

public NoSongsException(String s) {
	super(s);

	//	this.cause = cause;
	this.message = s;

}
}
