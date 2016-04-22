package controllers;

import com.litle.sdk.LitleOnline;
import com.litle.sdk.LitleOnlineException;
import com.litle.sdk.generate.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.confirmation;
import views.html.payments;

/**
 * Created by brian on 4/20/16.
 */
public class PaymentsController extends Controller {
    //public static final String className = PaymentsController.class.getName();

    private LitleOnline litleOnline;

    public Result payments() {
        Html html = new Html("<title>Sample content HTML</title>");
        return ok(payments.render("Sample Payment Entry Form", html));
    }

    public Result submitPayment() {
        DynamicForm form = Form.form().bindFromRequest();
        String response = handleVantivProcessing(form);
        StringBuffer sb = new StringBuffer();
        sb.append("<h3>Vantiv response</h3> ").append("<p>")
                .append(response).append("</p>");
        Html html = new Html(sb.toString());

        if (form.data().size() == 0) {
            return badRequest("Expecting some data");
        } else {
            return ok(confirmation.render("Success!",html));
        }
    }

    private String handleVantivProcessing(DynamicForm form) throws LitleOnlineException {
        // handles XML generation and connection to Vantiv
        litleOnline = new LitleOnline();

        Authorization auth = new Authorization();

        auth.setReportGroup(form.get("Report Group"));
        auth.setOrderId(form.get("Order ID"));
        auth.setAmount(Long.parseLong(form.get("Amount")));
        auth.setOrderSource(OrderSourceType.ECOMMERCE);

        CardType card = new CardType();
        String mop = form.get("Method of Payment");

        if ("vi".equalsIgnoreCase(mop)) {
            card.setType(MethodOfPaymentTypeEnum.VI);
        } else if ("mc".equalsIgnoreCase(mop)) {
            card.setType(MethodOfPaymentTypeEnum.MC);
        } else if ("ax".equalsIgnoreCase(mop)) {
            card.setType(MethodOfPaymentTypeEnum.AX);
        } else if ("di".equalsIgnoreCase(mop)) {
            card.setType(MethodOfPaymentTypeEnum.DI);
        } else {
            card.setType(MethodOfPaymentTypeEnum.BLANK);
        }
        auth.setCard(card);

        card.setNumber(form.get("CC Number"));
        card.setExpDate(form.get("Exp Date"));

        AuthorizationResponse authResponse = litleOnline.authorize(auth);

        return  generateHTMLFromResponse(authResponse);
    }

    private String generateHTMLFromResponse(AuthorizationResponse authResponse) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><p>")
                .append("<strong>Auth Code: </strong>").append(authResponse.getAuthCode()).append("<br/>")
                .append("<strong>Auth Approved Amount: </strong>").append(authResponse.getApprovedAmount()).append("<br/>")
                .append("<strong>Vantiv Txn ID: </strong>").append(authResponse.getLitleTxnId()).append("<br/>")
                .append("<strong>Auth Message</strong>").append(authResponse.getMessage()).append("<br/>")
                .append("</p></div>");
        return sb.toString();
    }

}
