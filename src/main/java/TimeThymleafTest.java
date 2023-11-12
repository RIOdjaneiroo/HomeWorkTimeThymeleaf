

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@WebServlet("/timet")
public class TimeThymleafTest extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();

        JavaxServletWebApplication jswa =
                JavaxServletWebApplication.buildApplication(this.getServletContext());

        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(jswa);
        resolver.setPrefix("/WEB-INF/template/");  // Шлях до ваших Thymeleaf-шаблонів
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setHeader("Refresh", "1");   // для оновлення сторінки

        String timezoneParameter = req.getParameter("timezone");
        String lastTimezone;

        if (timezoneParameter != null && !timezoneParameter.isEmpty()) {
            // якщо є часовий пояс в параметрі, використовуємо його
            lastTimezone = timezoneParameter;

            // створюємо об'єкт Cookie для збереження часового поясу
            Cookie timezoneCookie = new Cookie("lastTimezone", lastTimezone);
            timezoneCookie.setMaxAge(10);   // встановити час життя куки на 10 секунд
            resp.addCookie(timezoneCookie);
        } else {
            // інакше перевіряємо куки
            Cookie[] cookies = req.getCookies();
            lastTimezone = TimeZone.getDefault().getID();  // значення часового поясу за замовчуванням

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("lastTimezone".equals(cookie.getName())) {
                        lastTimezone = cookie.getValue();
                        break;
                    }
                }
            }
        }

// використовуємо lastTimezone як часовий пояс для подальшої обробки
        ZoneId zoneId = ZoneId.of(lastTimezone);
        String formattedTime = LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + lastTimezone;

        Context context = new Context(req.getLocale());
        context.setVariable("formattedTime", formattedTime);

        engine.process("time_templ", context, resp.getWriter());
        resp.getWriter().close();
    }
}


