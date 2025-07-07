package br.edu.ifsp.consulta.dto;

import java.util.List;

public class PageResponseDTO<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private boolean first;
    private boolean last;
    private int numberOfElements;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    
    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }
    
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
    
    public int getNumberOfElements() { return numberOfElements; }
    public void setNumberOfElements(int numberOfElements) { this.numberOfElements = numberOfElements; }
} 