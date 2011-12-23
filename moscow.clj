(ns ru.lj.alamar.elegraph
  (:require spiral data image util))

(defn сумма [список] (apply + список))

(defn zip [keys vals]
  (loop [seqq []
         ks (seq keys)
         vs (seq vals)]
    (if (and ks vs)
      (recur (conj seqq [(first ks) (first vs)])
        (next ks) (next vs))
      seqq)))

(def данные (read-data "data/moscow-2011-12-07/data.csv"))
(def заголовки (first данные))
(def уики (rest данные))

(defn партии [уик]
  (subvec уик 19))

(def избиратели second)

(defn явка [уик]
  (/ (сумма (партии уик)) (избиратели уик)))

(def максимальная-явка (apply max (map явка уики)))

(println "Максимальная явка" максимальная-явка)

; Для более равномерного распределения
(defn явка-для-окна [явка]
  (Math/pow
    (- максимальная-явка явка)
    1.33))

(defn доля-ер [уик]
  (/ (nth уик 24) (сумма (партии уик))))

(def окно-явки (окно явка-для-окна (map явка уики)))
(def окно-ер (окно identity (map доля-ер уики)))

; 60x80 cm at 300 dpi
(def ширина 6000)
(def высота 8000)

(def высота-графика 6800)
(def высота-полоски 1000)

(def фон (цвет 0xFF 0xFF 0xFF))
(def цвет-сетки (цвет 0xDD 0xDD 0xDD))
(def цвет-цифр (цвет 0x9A 0x9A 0x9A))
(def большая-сетка 5)
(def малая-сетка 3)
(def размер-шрифта 72)

(def холст (create-image ширина высота фон))

(сетка холст ширина высота-графика (/ 1 20) большая-сетка малая-сетка цвет-сетки размер-шрифта цвет-цифр окно-явки окно-ер)

(подписать холст ширина высота цвет-цифр)

(def шаблон (spiral))

(def цвета-партий [
            (цвет 0x66 0x66 0xCC) ;; Справедливая Россия
            (цвет 0xFF 0xFF 0)    ;; ЛДПР
            (цвет 0x9A 0x33 0x9A) ;; Патриоты России
            (цвет 0xCC 0 0)       ;; КПРФ
            (цвет 0x33 0x99 0)    ;; Яблоко
            (цвет 0 0 0)          ;; Единая Россия
            (цвет 0x33 0x9A 0x9A) ;; Правое Дело
           ])

(образцы холст цвета-партий 325 5235)

(defn упорядочить-фракции [уик]
  (zip (партии уик) цвета-партий))

(defn сортировать-фракции [уик]
  (reverse (sort-by first (упорядочить-фракции уик))))

; (doseq [уик уики]
;   (println (first уик) "явка" (float (явка уик)) "доля ер" (float (доля-ер уик))
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

(save-image холст "moscow.png")


