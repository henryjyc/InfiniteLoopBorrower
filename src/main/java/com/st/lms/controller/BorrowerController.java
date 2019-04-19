package com.st.lms.controller;

import com.st.lms.dto.BkLoansBkAuthDTO;
import com.st.lms.dto.BkLoansBranchDTO;
import com.st.lms.exception.BadRequestException;
import com.st.lms.exception.NotFoundException;
import com.st.lms.models.BookLoans;
import com.st.lms.service.BorrowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrower")
public class BorrowerController {

	@Autowired
	BorrowerService borrowerService;
	
	@GetMapping("cardNo/{cardNo}")
	public List<BkLoansBkAuthDTO> getBorrowedBooks(@PathVariable int cardNo) throws NotFoundException{
		if(!borrowerService.cardNoExists(cardNo))
			throw new NotFoundException("Login", "borrower", cardNo);
		return borrowerService.getBorrowedBooks(cardNo);
	}
	
	//return list of branches where the borrower has borrowed books from
	@GetMapping("/cardNo/{cardNo}/libraries")
	public List<BkLoansBranchDTO> getAllBranches(@PathVariable int cardNo) throws NotFoundException{
		if(!borrowerService.cardNoExists(cardNo)) {
			throw new NotFoundException("Login", "borrower", cardNo);
		}
		return borrowerService.getBranchesWithBkLoans(cardNo);
	}
	
	@GetMapping("/cardNo/{cardNo}/libraries/{branchId}/books")
	public List<BkLoansBkAuthDTO> getAllLoans(@PathVariable int cardNo, @PathVariable int branchId) throws NotFoundException{
		if(!borrowerService.cardNoExists(cardNo)) {
			throw new NotFoundException("Login", "borrower", cardNo);
		}
		else if(!borrowerService.branchExists(branchId)) {
			throw new NotFoundException("Search", "library branch", branchId);
		}
		return borrowerService.getBooksFromBranch(cardNo, branchId);
	}
	
	//borrow a book
	@PostMapping("/cardNo/{cardNo}/checkout")
	public ResponseEntity<BookLoans> borrowBook(@PathVariable int cardNo, @RequestBody BookLoans loan) throws NotFoundException, BadRequestException{
		if(!borrowerService.cardNoExists(cardNo)) {
			throw new NotFoundException("Login", "borrower", cardNo);
		}
		else if(!borrowerService.branchExists(loan.getBranchId())) {
			throw new NotFoundException("Check out", "library branch", loan.getBranchId());
		}
		else if(!borrowerService.loanExists(cardNo, loan.getBranchId(), loan.getBookId())) {
			throw new NotFoundException("Check out", "book loan", loan.getBookId());
		}
		int noOfCopies = borrowerService.getNoOfCopies(loan.getBookId(), loan.getBranchId());
		if(noOfCopies < 1) {
			throw new BadRequestException("Check out failed. That library does not have any copies available.");
		}
		else {
			borrowerService.checkOutBook(loan.getBookId(), loan.getBranchId(), cardNo, noOfCopies);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}
	
	//return a book
	@DeleteMapping("/cardNo/{cardNo}/libraries/{branchId}/books/{bookId}")
	public ResponseEntity<BookLoans> returnBook(@PathVariable int cardNo, @PathVariable int branchId, @PathVariable int bookId) throws NotFoundException, BadRequestException{
		if(!borrowerService.cardNoExists(cardNo)) {
			throw new NotFoundException("Login", "borrower", cardNo);
		}
		else if(!borrowerService.branchExists(branchId)) {
			throw new NotFoundException("Return", "library branch", branchId);
		}
		else if(!borrowerService.loanExists(cardNo, branchId, bookId)) {
			throw new NotFoundException("Return", "book loan", bookId);
		}
		if(!borrowerService.loanExists(cardNo, branchId, bookId)) {
			throw new BadRequestException("Return failed. You do not have that book checked out!");
		}
		else {
			int noOfCopies = borrowerService.getNoOfCopies(bookId, branchId);
			borrowerService.returnBook(bookId, branchId, cardNo, noOfCopies);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}
}