#lang racket

(define people (list))

(define (askContinue)
  (display "계속 입력하시겠습니까?(Y/N)")
  (define sel (read))
  (cond
    ((eq? sel 'Y) #t)
    ((eq? sel 'N) #f)
    (else (print "대문자 Y나 N을 입력하세요") (askContinue))
  )
)

(define (insert val lst)
  (cond
    ((null? lst) (cons val '()))
    ((> (car (cdr val)) (car (cdr (car lst)))) (cons val lst))
    (else (cons (car lst) (insert val (cdr lst))))
  )
)

(define (input)
  (display "성명 : ")
  (define name (read))
  (display "나이 : ")
  (define age (read))

  (define person (list name age))
  (set! people (insert person people))

  (display people)

  (define isContinue (askContinue))
  (cond
      (isContinue (input))
      (else (display "종료합니다."))
  )
)
(input)
