package com.petmilyday.dto.community;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class PageResponseDTO<E> {
    private int page;
    private int size;
    private int total;
    private int start;
    private int end;
    private boolean prev;
    private boolean next;
    private List<E> dtoList;

    @Builder(builderMethodName = "withAll") //메소드 호출 시 withAll로 받겠다
    public PageResponseDTO(PageRequestDTO pageRequestDTO, int total, List<E> dtoList) {
        if(total <= 0) {
            return;
        }
        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;
        this.dtoList = dtoList;
        this.end = (int)(Math.ceil(this.page / 10.0 )) * 10;
        //시작 페이지
        this.start = this.end - 9;
        //마지막 페이지
        int last = (int)(Math.ceil((total/(double)size)));
        //end가 last 값보다 작은 경우 last 값이 end
        this.end = end > last ? last: end;
        //이전 페이지
        this.prev = this.start > 1;
        //다음 페이지
        this.next = total > this.end * this.size;
    }
}
