package online.ityura.springdigitallibrary.service;

import lombok.RequiredArgsConstructor;
import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookMessageService {

    private static final String PROMPT_TEMPLATE = """
            Представь что ты прочитал книгу, о которой дальше пойдет речь. Ответь на вопрос, который будет указан ниже и расскажи как ты получил эту книгу
            (купил, нашел, выиграл, подарили, украл, взял напрокат, скачал, приснилась во сне, выбери в общем любой вариант или придумай свой),
            расскажи пару фактов об авторе и самой книге, о своих впечатлениях и мыслях. В общем поделись мнением.
            Данный ответ должен быть с юмором. Чтобы читающему было весело и интересно читать и желательно было смешно.
            В ответе обязательно хотя бы один раз должно присутствовать название и имя автора так как оно написано.
            Придумай себе имя, как будто ты реальный человек. Отвечай на языке, на котором задали вопрос.
            Если вопрос не касается книги или литературы, то скажи об этом и предложи задать нормальный вопрос.
            Вот инфа о книге: Название - {title}, Автор - {author}, Описание - {description} из таблицы book, Жанр - {genre}.
            Вопрос: {question}
            """;
    private final BookRepository bookRepository;
    private final OpenAIService openAIService;

    public String sendMessageToReader(Long bookId, String message) {
        // Получаем книгу с автором
        Book book = bookRepository.findByIdWithAuthor(bookId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found with id: " + bookId
                ));

        // Формируем промпт
        String genreDisplay = book.getGenre() != null ? book.getGenre().getDisplayName() : "Не указан";
        String description = book.getDescription() != null ? book.getDescription() : "Описание отсутствует";

        String prompt = PROMPT_TEMPLATE
                .replace("{title}", book.getTitle())
                .replace("{author}", book.getAuthor().getFullName())
                .replace("{description}", description)
                .replace("{genre}", genreDisplay)
                .replace("{question}", message);

        // Отправляем запрос в OpenAI и получаем ответ
        return openAIService.sendPromptAndGetResponse(prompt);
    }
}

