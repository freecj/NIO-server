/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 3 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.logging.Logger;

import G8R.serialization.CookieList;
import G8R.serialization.G8RResponse;
import G8R.serialization.ValidationException;

/**
 * Send SendGuess command Response message
 */
public class G8RSendGuess extends PollState {
	private String strNum = "Num";
	private String strScore = "Score";

	/**
	 * @param clntChan
	 * @param clientSocket
	 * @param logger
	 */
	public G8RSendGuess(AsynchronousSocketChannel clntChan, Logger logger) {
		super(clntChan, logger);

	}

	@Override
	public void generateMsg() {
		// get the cookielist from the request message
		CookieList beforeCookie = g8rRequest.getCookieList();

		try {
			if (functionNameForSendGuess.equals(g8rRequest.getFunction()) && g8rRequest.getParams().length == 1) {
				// SendGuess command and length of param are right
				String guessNum = g8rRequest.getParams()[0];
				if (!isNumeric(guessNum)) {
					// guessNum is not numeric
					g8rResponse = new G8RResponse(statusError, functionNameForNull, "malparam guessNum is not numeric",
							beforeCookie);
					context.setEndFlag();
					// generateErrorMsg("malparam guessNum is not numeric");
					return;
				}

				if (beforeCookie.findName(strNum) && beforeCookie.findName(strScore)) {
					int setValue = Integer.parseInt(beforeCookie.getValue(strNum));
					int numRequest = Integer.parseInt(guessNum);
					if (setValue >= 0 && setValue < 10 && numRequest >= 0 && numRequest < 10) {

						int score = Integer.parseInt(beforeCookie.getValue(strScore));
						if (numRequest == setValue) {
							// correct case
							score += 1;
							beforeCookie.add(strScore, String.valueOf(score));
							g8rResponse = new G8RResponse(statusOk, functionNameForNull,
									"Correct! Score=" + String.valueOf(score), beforeCookie);
							context.setEndFlag();
							// writerMsg();
							// close();
						} else if (numRequest < setValue) {
							// small case
							beforeCookie.add(strScore, String.valueOf(score));
							g8rResponse = new G8RResponse(statusError, functionNameForSendGuess,
									"Guess too low. Guess (0-9)?>", beforeCookie);
							// writerMsg();
						} else if (numRequest > setValue) {
							// large case
							beforeCookie.add(strScore, String.valueOf(score));
							g8rResponse = new G8RResponse(statusError, functionNameForSendGuess,
									"Guess too high. Guess (0-9)?>", beforeCookie);
							// writerMsg();
						}
					} else {
						// cookie value of Num is not right
						g8rResponse = new G8RResponse(statusError, functionNameForNull,
								"malfunction, Num is beyond the range 0-9", beforeCookie);
						context.setEndFlag();
						// generateErrorMsg("malfunction, Num is beyond the range 0-9");
					}
				} else {
					// no cookiename=Num
					g8rResponse = new G8RResponse(statusError, functionNameForNull, "malfunction", beforeCookie);
					context.setEndFlag();
					// generateErrorMsg("malfunction");

				}

			} else if (functionNameForFood.equals(g8rRequest.getFunction()) && g8rRequest.getParams().length != 1) {
				// resend the SendGuess command
				String mString = "Poorly formed Guess. Guess (0-9)?>";
				g8rResponse = new G8RResponse(statusError, functionNameForSendGuess, mString, beforeCookie);

				// writerMsg();
			} else {
				// error function name
				g8rResponse = new G8RResponse(statusError, functionNameForNull, "Unexpected message", beforeCookie);
				context.setEndFlag();
				// generateErrorMsg("Unexpected message");

			}

		} catch (ValidationException e) {
			close();
		} catch (Exception e) {
			close();
		}
	}

}
