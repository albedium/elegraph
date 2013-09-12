(ns ru.lj.alamar.elegraph
  (:require spiral data image util))

(def данные (read-data "data/moscow-2013-09-08/data.csv"))
(def заголовки (first данные))
(def уики (rest данные))

(defn фракции [уик]
  (subvec уик 18))

(def избиратели second)

(defn явка [уик]
  (/ (сумма (фракции уик)) (избиратели уик)))

(def максимальная-явка (apply max (map явка уики)))

(println "Максимальная явка" максимальная-явка)

(def средняя-явка 0.3)
(def среднее-ер 0.5)

(def разрыв 0.1)
(def степень 0.1)
; Для более равномерного распределения
(defn явка-для-окна [явка]
  (let [^double разница (- явка средняя-явка)
        сдвиг (Math/pow (+ разрыв (Math/abs разница)) степень)]
    (if (< явка средняя-явка)
      (+ (- 1 (Math/pow разрыв степень)) сдвиг)
      (- (+ 1 (Math/pow разрыв степень)) сдвиг))))

(defn ер-для-окна [ер]
  (let [^double разница (- ер среднее-ер)
        сдвиг (Math/pow (+ разрыв (Math/abs разница)) степень)]
    (if (> ер среднее-ер)
      (+ (- 1 (Math/pow разрыв степень)) сдвиг)
      (- (+ 1 (Math/pow разрыв степень)) сдвиг))))

(defn доля-ер [уик]
  (/ (last уик) (сумма (фракции уик))))

(def окно-явки (окно явка-для-окна (map явка уики)))
(def окно-ер (окно ер-для-окна (map доля-ер уики)))

; 60x80 cm at 300 dpi
(def ширина 4800)
(def высота 6400)

(def высота-графика 5200)
(def высота-полоски 1000)

(def фон (цвет 0xFF 0xFF 0xFF))
(def цвет-сетки (цвет 0xDD 0xDD 0xDD))
(def цвет-цифр (цвет 0x9A 0x9A 0x9A))
(def большая-сетка 5)
(def малая-сетка 3)
(def размер-шрифта 72)

(def холст (create-image ширина высота фон))

(сетка холст ширина высота-графика (/ 1 20) большая-сетка малая-сетка цвет-сетки размер-шрифта цвет-цифр окно-явки окно-ер)

(подписать холст цвет-цифр
  (fn [рисунок]
    (вывести-текст рисунок "программирование, идея — alamar.lj.ru" 72 (/ ширина 2) высота false true)
    (вывести-текст рисунок "Голосование на участках Москвы" 112 2600 350 false false)
    (вывести-текст рисунок "на выборах Мэра Москвы 8 сентября 2013" 72 2800 650 false false)
    (вывести-текст рисунок "явка →" 72 300 65 false false)
    (вывести-текст рисунок "2500 голосов, отданные за кандидатов:" 72 3000 1200 false false)
    (вывести-текст рисунок "Дегтярева Михаила Владимировича" 72 3200 1400 false false)
    (вывести-текст рисунок "Левичева Николая Владимировича" 72 3200 1600 false false)
    (вывести-текст рисунок "Мельникова Ивана Ивановича" 72 3200 1800 false false)
    (вывести-текст рисунок "Митрохина Сергея Сергеевича" 72 3200 2000 false false)
    (вывести-текст рисунок "Навального Алексея Анатольевича" 72 3200 2200 false false)
    (вывести-текст рисунок "Собянина Сергея Семеновича" 72 3200 2400 false false)
    (повернуть рисунок)
    (вывести-текст рисунок "доля голосов, отданных Собянину →" 72 180 -230 false false)))

(def шаблон (spiral))

(def цвета-фракций [
            (цвет 0xFF 0xFF 0)    ;; Дегтярев
            (цвет 0x66 0x66 0xCC) ;; Левичев
            (цвет 0xCC 0 0)       ;; Мельников
            (цвет 0x33 0x99 0)    ;; Митрохин
            (цвет 0x9A 0x33 0x9A) ;; Навальный
            (цвет 0 0 0)          ;; Собянин
;;          (цвет 0x33 0x9A 0x9A) ;; Правое Дело
           ])

(образцы холст цвета-фракций 3125 1435)

(defn упорядочить-фракции [уик]
  (zip (фракции уик) цвета-фракций))

(defn сортировать-фракции [уик]
  (reverse (sort-by first (упорядочить-фракции уик))))

; (doseq [уик уики]
;   (println (first уик) "явка" (float (явка уик)) "доля Собянина" (float (доля-ер уик))
;     "окно" (float (window-weight (явка-для-окна уик) окно-явки)) (float (window-weight (доля-ер уик) окно-ер))))

(полоска холст высота-графика высота-полоски
  (for [уик уики]
    (let [смещение (int (* ширина (- 1 (окно-явки (явка уик)))))]
      [смещение (упорядочить-фракции уик)])))

(сетка-для-полоски холст ширина высота-графика высота-полоски большая-сетка цвет-сетки (/ 1 20) окно-явки (/ 1 10) (окно identity [0 1]))

(println "Полоска нарисована")

(нанести холст 0 [ширина высота]
  (for [уик (take 4000 уики)]
    (let [смещение
          [(int (* ширина (- 1 (окно-явки (явка уик)))))
           (int (* высота-графика (окно-ер (доля-ер уик))))]]
      [смещение шаблон (сортировать-фракции уик)]))
  [фон цвет-сетки])

(println "Запись изображения")

(save-image холст "mosmer.png")


