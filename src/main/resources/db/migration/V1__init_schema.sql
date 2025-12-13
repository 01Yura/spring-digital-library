-- ============================================
-- ТРИГГЕР: Запрет удаления книги с deletion_locked = true
-- ============================================

CREATE OR REPLACE FUNCTION prevent_deletion_locked_book()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.deletion_locked = true THEN
        RAISE EXCEPTION 'Cannot delete book: deletion is locked';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_deletion_locked_before_delete
    BEFORE DELETE ON books
    FOR EACH ROW
    EXECUTE FUNCTION prevent_deletion_locked_book();

-- ============================================
-- ТРИГГЕР: Пересчет рейтинга книги при изменении ratings
-- ============================================

CREATE OR REPLACE FUNCTION recalculate_book_rating()
RETURNS TRIGGER AS $$
DECLARE
    avg_rating NUMERIC(3,2);
    rating_count INTEGER;
BEGIN
    -- Вычисляем средний рейтинг и количество
    SELECT 
        COALESCE(AVG(value), 0),
        COUNT(*)
    INTO avg_rating, rating_count
    FROM ratings
    WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    
    -- Обновляем книгу
    UPDATE books
    SET 
        rating_avg = avg_rating,
        rating_count = rating_count,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = COALESCE(NEW.book_id, OLD.book_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Триггер на INSERT
CREATE TRIGGER recalculate_rating_on_insert
    AFTER INSERT ON ratings
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_book_rating();

-- Триггер на UPDATE
CREATE TRIGGER recalculate_rating_on_update
    AFTER UPDATE ON ratings
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_book_rating();

-- Триггер на DELETE
CREATE TRIGGER recalculate_rating_on_delete
    AFTER DELETE ON ratings
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_book_rating();

-- ============================================
-- CONSTRAINT: Проверка значения рейтинга (1-10)
-- ============================================

ALTER TABLE ratings
ADD CONSTRAINT check_rating_value
CHECK (value >= 1 AND value <= 10);

