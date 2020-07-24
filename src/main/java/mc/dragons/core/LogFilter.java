package mc.dragons.core;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

public class LogFilter implements Filter {
	private State state;
	
	private Result process(String message) {
		if(!message.contains("issued server command: /syslogon")) return Result.NEUTRAL;
		Dragons.getInstance().getLogger().info(message.substring(0, message.indexOf(" ")) + " accessed the System Logon Authentication Service.");
		return Result.DENY;
	}
	
	@Override public Result filter(LogEvent record) {
		if(record == null) return Result.NEUTRAL;
		if(record.getMessage() == null) return Result.NEUTRAL;
		String entry = record.getMessage().getFormattedMessage();
		return process(entry);
	}
	
	@Override public Result getOnMatch() { return Result.NEUTRAL; }
	@Override public Result getOnMismatch() { return Result.NEUTRAL; }
	
	@Override public State getState() { return state; }
	@Override public void initialize() { state = State.INITIALIZED; }
	@Override public boolean isStarted() { return state == State.STARTED; }
	@Override public boolean isStopped() { return state == State.STOPPED; }
	@Override public void start() { state = State.STARTED; }
	@Override public void stop() { state = State.STOPPED; }

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object... arg4) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, Object arg3, Throwable arg4) {
		return Result.NEUTRAL;
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, Message arg3, Throwable arg4) {
		return process(arg3.getFormattedMessage());
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8,
			Object arg9) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8,
			Object arg9, Object arg10) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8,
			Object arg9, Object arg10, Object arg11) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8,
			Object arg9, Object arg10, Object arg11, Object arg12) {
		return process(arg3);
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, org.apache.logging.log4j.Level arg1,
			Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8,
			Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) {
		return process(arg3);
	}
}
