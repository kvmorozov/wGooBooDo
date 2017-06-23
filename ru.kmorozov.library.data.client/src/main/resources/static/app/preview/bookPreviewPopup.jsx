import React from "react";
import Modal from "react-bootstrap/lib/Modal";
import {Button} from "semantic-ui-react";
import client from "../restClient";
import PDF from "react-pdf-js";

class BookPreviewPopup extends React.Component {

    constructor(props) {
        super(props);

        this.state = {showModal: false};
    }

    close = () => {
        this.setState({showModal: false});
    }

    open = (book) => {
        this.setState({showModal: true, book: book});
    }

    preview = () => {
        if (this.state.book != undefined)
            client({
                method: 'GET', path: '/downloadBook/' + this.state.book.id
            }).then(
                response => {
                    return response.entity;
                }).done(book => {
                this.setState({book: book});
            })
    }


    onDocumentComplete = (pages) => {
        this.setState({page: 1, pages});
    }

    onPageComplete = (page) => {
        this.setState({page});
    }

    handlePrevious = () => {
        this.setState({page: this.state.page - 1});
    }

    handleNext = () => {
        this.setState({page: this.state.page + 1});
    }

    renderPagination(page, pages) {
        let previousButton = <li className="previous" onClick={this.handlePrevious}><a href="#"><i
            className="fa fa-arrow-left"></i> Previous</a></li>;
        if (page === 1) {
            previousButton =
                <li className="previous disabled"><a href="#"><i className="fa fa-arrow-left"></i> Previous</a></li>;
        }
        let nextButton = <li className="next" onClick={this.handleNext}><a href="#">Next <i
            className="fa fa-arrow-right"></i></a></li>;
        if (page === pages) {
            nextButton = <li className="next disabled"><a href="#">Next <i className="fa fa-arrow-right"></i></a></li>;
        }
        return (
            <nav>
                <ul className="pager">
                    {previousButton}
                    {nextButton}
                </ul>
            </nav>
        );
    }

    render() {
        let title = this.state.book != undefined ? this.state.book.title : "";
        let preview = <div/>;
        let pagination = null;
        if (this.state.pages) {
            pagination = this.renderPagination(this.state.page, this.state.pages);
        }

        if (this.state.book != undefined)
            if (this.state.book.localPath && this.state.book.format == 'PDF')
                preview = <div>
                    <PDF file={this.state.book.localPath} onDocumentComplete={this.onDocumentComplete}
                         onPageComplete={this.onPageComplete} page={this.state.page}/>
                    {pagination}
                </div>

        if (this.state.showModal)
            return (
                <Modal show={this.state.showModal} onHide={this.close} backdrop="static">
                    <Modal.Header closeButton>
                        <Modal.Title>Book preview</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {title}
                        {preview}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.preview}>Preview</Button>
                        <Button onClick={this.close}>Close</Button>
                    </Modal.Footer>
                </Modal>)
        else
            return (<div/>);
    }
}

export default BookPreviewPopup;