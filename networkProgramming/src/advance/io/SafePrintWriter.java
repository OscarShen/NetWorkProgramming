package advance.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class SafePrintWriter extends Writer {
	protected Writer out;
	private boolean autoFlush = false;
	private String lineSeparator;
	private boolean closed = false;

	public SafePrintWriter(Writer out, String lineSeparator) {
		this(out, false, lineSeparator);
	}

	public SafePrintWriter(Writer out, char lineSeparator) {
		this(out, false, String.valueOf(lineSeparator));
	}

	public SafePrintWriter(Writer out, boolean autoFlush, String lineSeparator) {
		super(out);
		this.out = out;
		this.autoFlush = autoFlush;
		if (lineSeparator == null)
			throw new NullPointerException();
		this.lineSeparator = lineSeparator;
	}

	public SafePrintWriter(OutputStream out, boolean autoFlush, String encoding, String lineSeparator)
			throws UnsupportedEncodingException {
		this(new OutputStreamWriter(out, encoding), autoFlush, lineSeparator);
	}

	@Override
	public void flush() throws IOException {
		synchronized (lock) {
			if (closed)
				throw new IOException("Stream closed");
			out.flush();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.flush();
		} catch (IOException e) {
		}
		synchronized (lock) {
			out.close();
			this.closed = true;
		}
	}

	@Override
	public void write(int c) throws IOException {
		synchronized (lock) {
			if (closed)
				throw new IOException("Stream Closed");
			out.write(c);
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		synchronized (lock) {
			if (closed)
				throw new IOException("Stream Closed");
			out.write(cbuf, off, len);
		}
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		synchronized (lock) {
			if (closed)
				throw new IOException("Stream Closed");
			out.write(cbuf, 0, cbuf.length);
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		synchronized (lock) {
			if (closed)
				throw new IOException("Stream Closed");
			out.write(str, off, len);
		}
	}

	public void print(boolean b) throws IOException {
		if (b)
			this.write("true");
		else
			this.write("false");
	}

	public void println(boolean b) throws IOException {
		if (b)
			this.write("true");
		else
			this.write("false");
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(char c) throws IOException {
		this.write(String.valueOf(c));
	}

	public void println(char c) throws IOException {
		this.write(String.valueOf(c));
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(int i) throws IOException {
		this.write(String.valueOf(i));
	}

	public void println(int i) throws IOException {
		this.write(String.valueOf(i));
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(long l) throws IOException {
		this.write(String.valueOf(l));
	}

	public void println(long l) throws IOException {
		this.write(String.valueOf(l));
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(float f) throws IOException {
		this.write(String.valueOf(f));
	}

	public void println(float f) throws IOException {
		this.write(String.valueOf(f));
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(double d) throws IOException {
		this.write(String.valueOf(d));
	}

	public void println(double d) throws IOException {
		this.write(String.valueOf(d));
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(char[] cbuf) throws IOException {
		this.write(cbuf);
	}

	public void println(char[] cbuf) throws IOException {
		this.write(cbuf);
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(String s) throws IOException {
		this.write(s);
	}

	public void println(String s) throws IOException {
		if (s == null)
			this.write("null");
		else
			this.write(s);
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void print(Object o) throws IOException {
		if (o == null)
			this.write("null");
		else
			this.write(o.toString());
	}

	public void println(Object o) throws IOException {
		if (o == null)
			this.write("null");
		else
			this.write(o.toString());
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}

	public void println() throws IOException {
		this.write(lineSeparator);
		if (autoFlush)
			out.flush();
	}
}
