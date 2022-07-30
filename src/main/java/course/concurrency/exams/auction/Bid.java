package course.concurrency.exams.auction;

public class Bid {
    // учебный пример без private модификаторов и get методов
    Long id; // ID заявки
    Long participantId; // ID участника
    Long price; // предложенная цена

    public Bid(Long id, Long participantId, Long price) {
        this.id = id;
        this.participantId = participantId;
        this.price = price;
    }
}
