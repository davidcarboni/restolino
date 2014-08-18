package com.github.davidcarboni.restolino.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class DebugStreamReader extends InputStreamReader {

	public DebugStreamReader(InputStream in, String cs)
			throws UnsupportedEncodingException {
		super(in, cs);
	}

	@Override
	public int read() throws IOException {
		int result = super.read();
		System.out.print((char) result);
		return result;
	}

	@Override
	public int read(char[] cbuf, int offset, int length) throws IOException {
		int result = super.read(cbuf, offset, length);
		for (int i = 0; i < length && i < cbuf.length; i++)
			System.out.print(cbuf[i]);
		return result;
	}

	@Override
	public void close() throws IOException {
		int c;
		while ((c = read()) != -1)
			System.out.print((char) c);
		super.close();
	}

}
