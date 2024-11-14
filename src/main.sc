require: slotfilling/slotFilling.sc
    module = sys.zb-common
  
require: text/text.sc
    module = sys.zb-common

require: common.js
    module = sys.zb-common
    
# Для игры Назови столицу    
require: where/where.sc
    module = sys.zb-common
    
require:  GuessTheCity.js
    

theme: /

    state: Start || modal = True 
        q!: $regex</start>
        script:
            $jsapi.startSession();
            $context.session = {};
            $context.client = {};
            $session.count = -1; 
            $session.game_started = false;
        a: Привет! Я бот для игры в "Угадай столиицу". 
        go!: /Приветствие

    state: Приветствие
        a: Хочешь сыграть?
        script: 
            $session.count += 1

        
        state: Согласие
            intent: /Согласие
            script: 
                $session.game_started = true;
            go!: /Игра
            
        state: Несогласие
            intent: /Несогласие
            a: Жаль, я больше ничего не умею :(
            go!: /GameAborted
            
    
    state: Игра
        script:
            $session.keys = Object.keys($Geography);    
            var place = $Geography[chooseRandCountryKey($session.keys)]
            var country = place.value.country
            $session.country = place.value.country // для GPT 
            $session.capital = place.value.name
            $reactions.answer("Назови столицу государства " + country)
        go!: /CheckCityName  
        
        
    state: CheckCityName
        state: CityPattern
            q: * $City *
            script:
                if (isAFullNameOfCity()) {
                    var answer = $parseTree._City.name;
                    if (answer == $session.capital && $session.count == 4){
                        $session.count += 1
                        $reactions.answer("Ты прав, столица действительно " + $session.capital)
                        $reactions.transition( {value: "/FunFact", deferred: false})
                    } else if (answer == $session.capital && ($session.count != null && $session.count % 5 == 0 )) { // Каждый пятый правильынй ответ выводим факт
                        $session.count += 1
                        $reactions.answer("Ты прав, столица действительно " + $session.capital)
                        $reactions.transition( {value: "/FunFact", deferred: false})
                    } else if (answer == $session.capital){
                        $session.count += 1
                        $reactions.answer("Ты прав, столица действительно " + $session.capital);
                    } else $reactions.answer("Нет, столица  " + $session.capital)
                } else $reactions.answer("Используйте только полные названия городов")

            go!: /Игра
        
        state: NoMatch || noContext = true
            event: noMatch
            a: Я не знаю такого города. Попробуйте ввести другой город
        
    state: FunFact
        script:
            var factObject = [$session.country, $session.capital] // Случайным образом выбираем про столицу или про страну мы выведем факт
            var randomIndex = Math.floor(Math.random() * factObject.length);
            var randomElement = factObject[randomIndex]
            var messageText = stringConcat("Расскажи интересный факт о государстве", randomElement)
            var assistantResponse = $gpt.createChatCompletion([{ "role": "user", "content": messageText}]);
            var response = assistantResponse.choices[0].message.content;
            $reactions.answer(response);
        go!: /Игра
    
    state: EndGame
        intent!: /endThisGame
        script: 
            if ($session.count == null) {
                    $reactions.answer("Твой счёт: 0") 
                } else $reactions.answer("Твой счёт: " + $session.count) 
            $reactions.answer('Если хотите сыграть, напишите "Давай сыграем"')
            $session.count = 0;

    state: GameAborted
        a: Если хотите сыграть, напишите "Давай сыграем"
            
    state: StartGame
        intent!: /Согласие
        go!: /Игра
        
    state: Rules || noContext = true
        q!: *правил*
        a: Я буду называть тебе страну, а ты должен назвать её столицу. За каждый правильный ответ ты получаешь 1 балл. Если ответ неверный, то я загадываю следующее слово. Если ты хочешьзакончить игру, напиши об этом. P.S. Пожалуйста, используй полные названия городов.
        # a: За каждый правильный ответ ты получаешь 1 балл
        # a: Если ответ неверный, то я загадываю следующее слово.
        # a: Если ты хочешьзакончить игру, напиши об этом.
        # a: P.S. Пожалуйста, используй полные названия городов.
        go!: /Игра
    
        

    state: NoMatch
        event!: noMatch
        a: Я не понял. Вы сказали: {{$request.query}}
        a: Если хотите сыграть, напишите "Давай сыграем"
    
    state: reset
        q!: reset
        script:
            $session = {};
            $client = {};
        go!: /Start

