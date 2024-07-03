package br.com.bluesburguer.production.domain.exception;

public class CpfInvalidException extends RuntimeException {

	private static final long serialVersionUID = -7404077233577007012L;
	
	public CpfInvalidException() {
		super();
	}
	
	public CpfInvalidException(Exception e) {
		super(e);
	}

}
